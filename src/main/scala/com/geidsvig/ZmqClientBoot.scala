package com.geidsvig

import org.zeromq.ZMQ
import org.zeromq.ZMQ.Context
import akka.actor.ActorSystem
import akka.actor.Props
import com.geidsvig.parallel.ZMQRequirements
import com.geidsvig.zmq.DealerPollerImpl
import com.geidsvig.zmq.BrokerDealerRequirements
import com.geidsvig.zmq.Envelope
import akka.actor.Actor
import java.util.Date

/**
 * Bootstrap for ZmqClient Akka microkernel.
 */
class ZmqClientBoot extends akka.kernel.Bootable {
  val config = com.typesafe.config.ConfigFactory.load()
  val system = ActorSystem("zmqpClientSystem", config)
  
  val log = system.log

  printf("Version string: %s, Version int: %d\n", ZMQ.getVersionString, ZMQ.getFullVersion)

  val serverConnection = system.settings.config.getString("zmq.server.connection")//"tcp://vagrant-zmq-server:5559"

  val msgs = system.settings.config.getInt("zmq.numberOfMessages") // 1000000
  
  trait BrokerDealerDependencies extends BrokerDealerRequirements {
    val zmqContext: Context = ZMQ.context(1)
  }
  val dealerPoller = system.actorOf(Props(new DealerPollerImpl(serverConnection, 500) with BrokerDealerDependencies))
  val rcvActorRef = system.actorOf(Props(new Actor {
    var msgCounter = 0
    var start = System.currentTimeMillis
    def receive = {
      case msg: Envelope => {
        // log info("Got message back from DealerPoller " + msg.toString)
        msgCounter += 1
        if (msgCounter >= msgs) {
          log info("Round trip message counter: " + msgCounter)
          val end = System.currentTimeMillis
          val duration = (end - start)
          log info("Time to round trip " + msgs + " messages : " + duration)
          log info ("Messages per second " + (msgs / duration * 1000))
        }
      }
      case 'reset => {
        msgCounter = 0
        start = System.currentTimeMillis
      }
      case 'printresults => log info("Round trip message counter: " + msgCounter)
      case other => log error("Unsupported message " + other.toString)
    }
  }))

  def startup = {
    reset
    testZmq
  }
  
  def testZmq {
    (1 to msgs) map { msgCount =>
      dealerPoller ! Envelope("no_action", Map("actorRef" -> rcvActorRef.path.toStringWithAddress(rcvActorRef.path.address)), "some body of text for test message "+msgCount)
    }
  }
  
  def reset {
    rcvActorRef ! 'reset
  }
  
  def checkStatus {
    rcvActorRef ! 'printresults
  }

  def shutdown = {
    system.shutdown()
  }
}

