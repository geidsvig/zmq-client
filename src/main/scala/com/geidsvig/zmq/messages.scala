package com.geidsvig.zmq

case class Envelope(action: String, header: Map[String, String], payload: String)

object Envelope {
  val SPLITTER = "=>"
  
  def envelopeToFrames(envelope: Envelope): List[String] = {
    List("S_ACTION", envelope.action, "S_HEADER") ++
      envelope.header.map { pair => pair._1 + SPLITTER + pair._2 }.toList ++
      List("S_PAYLOAD", envelope.payload)
  }
  
  def framesToEnvelope(frames: List[String]): Envelope = {
    // terrible assumption that we always have this order...
    val actionIndex = frames.indexOf("S_ACTION")
    val headerIndex = frames.indexOf("S_HEADER")
    val payloadIndex = frames.indexOf("S_PAYLOAD")
    
    val action = frames.slice(actionIndex + 1, headerIndex).mkString("")
    val header = frames.slice(headerIndex + 1, payloadIndex).map { frame =>
      {
        val kv = frame.split(SPLITTER)
        kv(0) -> kv(1)
      }
    }.toMap
    val payload = frames.splitAt(payloadIndex + 1)._2.mkString("")
    Envelope(action, header, payload)
  }
  
}
