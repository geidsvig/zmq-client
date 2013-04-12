package com.geidsvig

/**
 * Bootstrap for ZmqClient Akka microkernel.
 */
class ZmqClientBoot extends akka.kernel.Bootable {
  def startup = {
    
    val config = com.typesafe.config.ConfigFactory.load()
    println("Boot successful")

  }

  def shutdown = {

  }
}

