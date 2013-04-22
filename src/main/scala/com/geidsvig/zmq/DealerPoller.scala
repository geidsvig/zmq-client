package com.geidsvig.zmq

import akka.actor.Actor
import akka.actor.ActorLogging

import org.zeromq.ZMQ
import org.zeromq.ZMQ.{ Context => ZMQContext }
import org.zeromq.ZMQ.{ Socket => ZMQSocket }
import org.zeromq.ZMQ.{ Poller => ZMQPoller }

trait BrokerDealerRequirements {
  val zmqContext: ZMQContext
}

/**
 * This DealerPoller class handles responses by forwarding to the actorRef specified in the Envelope header.
 *
 * @param url
 * @param pollerTimeout
 */
class DealerPollerImpl(url: String, pollerTimeout: Long) extends DealerPoller(url, pollerTimeout) {
  this: BrokerDealerRequirements =>

  def handleResponse(envelope: Envelope): Option[Error] = {
    // log info ("Handling ZMQ response: " + envelope)
    envelope.header.get("actorRef") match {
      case Some(actorRef) => {
        val ref = context.system.actorFor(actorRef)
        // log info ("ActorRef " + ref)
        ref ! envelope
        None
      }
      case None => Some(new Error("Handle Response was missing actorRef"))
    }
  }
}

/**
 * A DealerPoller actor that connects a single socket to the provided url and polls for inbound messages.
 *
 * There must be one context per application. This context must be injected with the BrokerDealerRequirements trait.
 * There must only be one socket connection per thread, so this actor must only have one socket connection.
 *
 * Implementing classes of DealerPoller must not overwrite any methods, except for handleResponse(msg).
 *
 * @param url the fully qualified url, including connection type (tcp, inproc...), the host name, and port.
 * @param pollerTimeout the timeout in milliseconds for each poll
 */
abstract class DealerPoller(url: String, pollerTimeout: Long) extends Actor
  with ActorLogging {
  this: BrokerDealerRequirements =>

  var zmqSocket: ZMQSocket = _
  var zmqPoller: ZMQPoller = _

  val NOFLAGS = 0
  val ENDOFFRAMES = "END_OF_FRAMES"

  override def preStart() {
    zmqSocket = zmqContext.socket(ZMQ.DEALER)
    zmqSocket.connect(url)
    zmqSocket.setSndHWM(4000000)
    zmqSocket.setRcvHWM(4000000)
    zmqSocket.setLinger(1)
    zmqSocket.setReceiveTimeOut(500)
    zmqPoller = zmqContext.poller(1)
    zmqPoller.register(zmqSocket, ZMQ.Poller.POLLIN)

    self ! 'poll
  }

  override def postStop() {
    zmqPoller.unregister(zmqSocket)
    zmqSocket.disconnect(url)
    zmqSocket.close()
  }

  def receive = {
    case 'poll => {
      // log info ("polling...")
      zmqPoller.poll(pollerTimeout) match {
        case -1 => log error "Failed ZMQ poll" // TODO check errno for error message
        case 0 => {} // log info ("Nothing on the inbound zmq socket")
        case count => {
          // log info ("Something on inbound zmqp socket. Frame count: " + count)
          (1 to count) map { _ =>
            {
              val frames = zmqReceive()
              val envelope = Envelope.framesToEnvelope(frames)
              handleResponse(envelope) match {
                case Some(error) => log error (error.toString)
                case None => {} //log info ("Successfully handled response " + envelope)
              }
            }
          }
        }
      }
      self ! 'poll
    }
    case msg: Envelope => handleRequest(msg)
    case other => log warning ("Unsupported message %", other)
  }

  /**
   * Given a list of String, "frames", send over ZMQ.
   *
   * @param frames
   */
  private def zmqSend(frames: List[String]) {
    frames.map { frame => zmqSocket.send(frame.getBytes, ZMQ.SNDMORE) }
    zmqSocket.send(ENDOFFRAMES.getBytes, NOFLAGS)
  }

  /**
   * Poller indicated that inbound ZMQ frames exist in the queue.
   * This method takes the set of frames off of the queue and returns a list of frames.
   *
   * @returns list of frames
   */
  private def zmqReceive(): List[String] = {
    var frames: List[String] = List()
    var done = false
    while (!done) {
      Option(zmqSocket.recv(ZMQ.DONTWAIT)).map { s: Array[Byte] => new String(s) } match {
        case Some(frame) if (!frame.equals(ENDOFFRAMES)) => frames ::= frame
        case _ => done = true
      }
    }
    frames.reverse
  }

  /**
   * This method deals with outbound messages to the ZMQ socket.
   *
   * @param envelope
   */
  private def handleRequest(envelope: Envelope) {
    val frames = Envelope.envelopeToFrames(envelope)
    zmqSend(frames)
  }

  /**
   * This method deals with inbound messages on the ZMQ socket, collected from the poller.
   *
   * @param msg the inbound message from ZMQ socket connection
   */
  def handleResponse(envelope: Envelope): Option[Error]

}
