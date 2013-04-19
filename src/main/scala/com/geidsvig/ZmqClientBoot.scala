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

    // start up client

    //HelloWorldClient

    //new WeatherUpdateClient()

    /*
    trait ZMQDependencies extends ZMQRequirements {
      val zmqContext: Context = ZMQ.context(1)
    }
    // let's start 5
    system.actorOf(Props(new parallel.Worker("A") with ZMQDependencies)) ! 'start
    system.actorOf(Props(new parallel.Worker("B") with ZMQDependencies)) ! 'start
    system.actorOf(Props(new parallel.Worker("C") with ZMQDependencies)) ! 'start
    system.actorOf(Props(new parallel.Worker("D") with ZMQDependencies)) ! 'start
    system.actorOf(Props(new parallel.Worker("E") with ZMQDependencies)) ! 'start
    */

    val start = new Date
    val msgs = 2000
    
    trait BrokerDealerDependencies extends BrokerDealerRequirements {
      val zmqContext: Context = ZMQ.context(1)
    }
    val dealerPoller = system.actorOf(Props(new DealerPollerImpl("tcp://vagrant-zmq-server:5559", 500) with BrokerDealerDependencies))
    val rcvActorRef = system.actorOf(Props(new Actor {
      var msgCounter = 0
      def receive = {
        case msg: Envelope => {
          log info("Got message back from DealerPoller " + msg.toString)
          msgCounter += 1
          log info("Number of messages that made round trip: " + msgCounter)
          if (msgCounter >= msgs) {
            val end = new Date
            log info("Time to round trip " + msgs + " messages : " + (end.getTime() - start.getTime()))
          }
        }
        case other => log error("Unsupported message " + other.toString)
      }
    }))
    (1 to msgs) map { i =>
      dealerPoller ! Envelope("no_action", Map("actorRef" -> rcvActorRef.path.toStringWithAddress(rcvActorRef.path.address)), "some body of text for test message "+i)
    }

  }

  def shutdown = {

  }
}

