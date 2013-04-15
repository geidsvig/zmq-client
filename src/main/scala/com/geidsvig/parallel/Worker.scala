package com.geidsvig.parallel

/*
*  Task worker in Scala
*  Connects PULL socket to tcp://localhost:5557
*  Collects workloads from ventilator via that socket
*  Connects PUSH socket to tcp://localhost:5558
*  Sends results to sink via that socket
*
* @author Giovanni Ruggiero
* @email giovanni.ruggiero@gmail.com
*/

import org.zeromq.ZMQ
import akka.actor.Actor

class Worker(name: String) extends Actor {
  val zmqContext = ZMQ.context(1)

  println("Starting worker " + name)
  
  //  Socket to receive messages on
  val receiver = zmqContext.socket(ZMQ.PULL)
  receiver.connect("tcp://vagrant-zmq-server:5557")

  //  Socket to send messages to
  val zmqSender = zmqContext.socket(ZMQ.PUSH)
  zmqSender.connect("tcp://vagrant-zmq-server:5558")

  //  Process tasks forever
  while (true) {
    val string = new String(receiver.recv(0)).trim()
    val nsec = string.toLong
    //  Simple progress indicator for the viewer
    System.out.flush()
    print(string + '.')

    //  Do the work
    Thread.sleep(nsec)

    //  Send results to sink
    zmqSender.send("".getBytes(), 0)
  }
  
  def receive = {
    case _ => {}
  }

}