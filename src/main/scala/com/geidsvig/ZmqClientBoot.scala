package com.geidsvig

import org.zeromq.ZMQ
import akka.actor.ActorSystem
import akka.actor.Props

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
    
    // let's start 5
    val workerA = system.actorOf(Props(new parallel.Worker("A")))
    val workerB = system.actorOf(Props(new parallel.Worker("B")))
    val workerC = system.actorOf(Props(new parallel.Worker("C")))
    val workerD = system.actorOf(Props(new parallel.Worker("D")))
    val workerE = system.actorOf(Props(new parallel.Worker("E")))

  }

  def shutdown = {

  }
}

