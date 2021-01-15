package name.jchein.ptf.artlab.extensions.id_generator

import java.time.Instant

import akka.actor.typed.ActorRef
import akka.pattern.StatusReply


trait ULIDRandomBitsAuthority {
  sealed trait Message

  trait Request extends Message
  final case class LeaseULIDNodeRequest(
    val replyTo: ActorRef[StatusReply[ULIDNodeLeaseResponse]]
  ) extends Request
//  sealed trait LeaseULIDNodeRequest extends Request {
//    val replyTo: ActorRef[StatusReply[ULIDNodeLeaseResponse]]
//  }
  
  trait InternalMessage extends Message

  sealed trait Response 
  sealed trait ULIDNodeLeaseResponse extends Response {
    val variantId: Byte
    val nodeBits: Long
    val nodeBitCount: Int
    val epochBitCount: Int
    val seriesBitCount: Int
    val expiresAfter: Instant
  }
}

//class ULIDRandomBitsAuthority {
//  
//}
