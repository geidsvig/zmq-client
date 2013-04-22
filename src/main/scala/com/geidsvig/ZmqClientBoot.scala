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
  def startup = {

    val config = com.typesafe.config.ConfigFactory.load()
    val system = ActorSystem("zmqpClientSystem", config)
    val log = system.log

    printf("Version string: %s, Version int: %d\n", ZMQ.getVersionString, ZMQ.getFullVersion)

    val serverConnection = system.settings.config.getString("zmq.server.connection")//"tcp://vagrant-zmq-server:5559"

    val start = new Date
    val msgs = system.settings.config.getInt("zmq.numberOfMessages") // 1000000
    val sleepDuration = system.settings.config.getLong("zmq.batchWaitDuration") // 250
    val throughput = system.settings.config.getInt("zmq.throughput") // 1000
    
    trait BrokerDealerDependencies extends BrokerDealerRequirements {
      val zmqContext: Context = ZMQ.context(1)
    }
    val dealerPoller = system.actorOf(Props(new DealerPollerImpl(serverConnection, 500) with BrokerDealerDependencies))
    val rcvActorRef = system.actorOf(Props(new Actor {
      var msgCounter = 0
      def receive = {
        case msg: Envelope => {
          // log info("Got message back from DealerPoller " + msg.toString)
          msgCounter += 1
          if (msgCounter >= msgs) {
            log info("Round trip message counter: " + msgCounter)
            val end = new Date
            log info("Time to round trip " + msgs + " messages : " + (end.getTime() - start.getTime()))
          }
        }
        case 'printresults => log info("Round trip message counter: " + msgCounter)
        case other => log error("Unsupported message " + other.toString)
      }
    }))
    var msgCount = 0
    (1 to (msgs / throughput)) map { i =>
      (1 to throughput) map { j => {
        msgCount += 1
        dealerPoller ! Envelope("no_action", Map("actorRef" -> rcvActorRef.path.toStringWithAddress(rcvActorRef.path.address)), "some body of text for test message "+msgCount)
      }}
      //Thread.sleep(sleepDuration)
    }
    log info ("Done sending message envelopes")
    Thread.sleep(20000)
    log info ("after 20 seconds...")
    rcvActorRef ! 'printresults
    Thread.sleep(20000)
    log info ("after 40 seconds...")
    rcvActorRef ! 'printresults
    Thread.sleep(20000)
    log info ("after 60 seconds...")
    rcvActorRef ! 'printresults
    Thread.sleep(20000)
    log info ("after 80 seconds...")
    rcvActorRef ! 'printresults
    Thread.sleep(20000)
    log info ("after 100 seconds...")
    rcvActorRef ! 'printresults
    Thread.sleep(20000)
    log info ("after 120 seconds...")
    rcvActorRef ! 'printresults
    Thread.sleep(20000)
    log info ("after 140 seconds...")
    rcvActorRef ! 'printresults
    Thread.sleep(20000)
    log info ("after 160 seconds...")
    rcvActorRef ! 'printresults
    Thread.sleep(20000)
    log info ("after 180 seconds...")
    rcvActorRef ! 'printresults

  }

  def shutdown = {

  }
}

