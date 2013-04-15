package com.geidsvig

import org.zeromq.ZMQ
import org.zeromq.ZMQ.Context
import akka.actor.ActorSystem
import akka.actor.Props
import com.geidsvig.parallel.ZMQRequirements

/**
 * Bootstrap for ZmqClient Akka microkernel.
 */
class ZmqClientBoot extends akka.kernel.Bootable {
  def startup = {
    
    val config = com.typesafe.config.ConfigFactory.load()
    val system = ActorSystem("zmqpClientSystem", config)

    printf("Version string: %s, Version int: %d\n", ZMQ.getVersionString, ZMQ.getFullVersion)

    // start up client
    
    //HelloWorldClient
    
    //new WeatherUpdateClient()
    
    trait ZMQDependencies extends ZMQRequirements {
      val zmqContext: Context = ZMQ.context(1)
    }
    // let's start 5
    system.actorOf(Props(new parallel.Worker("A") with ZMQDependencies)) ! 'start
    system.actorOf(Props(new parallel.Worker("B") with ZMQDependencies)) ! 'start
    system.actorOf(Props(new parallel.Worker("C") with ZMQDependencies)) ! 'start
    system.actorOf(Props(new parallel.Worker("D") with ZMQDependencies)) ! 'start
    system.actorOf(Props(new parallel.Worker("E") with ZMQDependencies)) ! 'start

  }

  def shutdown = {

  }
}

