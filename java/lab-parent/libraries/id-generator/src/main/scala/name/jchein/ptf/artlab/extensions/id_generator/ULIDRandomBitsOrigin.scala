package name.jchein.ptf.artlab.extensions.id_generator

import java.time.Instant

import akka.actor.typed.ActorRef
import akka.pattern.StatusReply
import akka.compat.Future
import scala.concurrent.Promise
import scala.concurrent.Future
import akka.actor.typed.Behavior
import java.time.Duration
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorSystem
import java.util.regex.Pattern.All
import name.jchein.ptf.toyapp.main.PrintActor
import name.jchein.ptf.toyapp.main.BlockingActor




object ULIDRandomBitsOrigin {
//  final case class ClaimPolicy(baseDuration: Duration, jitter: Duration, reservedLimit: Int)
  final case class NodeValue(sequenceName: String, nodeId: Long)
  final case class NodeReservation(value: NodeValue, expiration: Instant)
  final case class NodeReplacement(oldValue: NodeValue, newValue: NodeValue, expiration: Instant)
  final case class SequenceVariant(
    // Split repeated policy metadata to separate Describe query protocol
    // step because a lot more was being repeated than added by the Lease
    // request's previous response.  This also permits using these details
    // earlier than an initial lease result would have provided them.
    name: String,
    variantId: Byte,
    nodeBitCount: Int,
    epochBitCount: Int,
    epochBitLimit: Int,
    seriesBitCount: Int,
  )
  
  sealed trait Message
  
  // An unsealed trait to be used as an extension point by concrete
  // implementations
  trait InternalMessage extends Message

  sealed trait Request extends Message
  final case class DescribeOriginRequest(
    val replyTo: ActorRef[StatusReply[DescribeOriginResponse]],
  ) extends Request
  final case class BeginLeaseRequest(
    val replyTo: ActorRef[StatusReply[BeginLeaseResponse]],
    val newReservationCount: Int
  ) extends Request
  final case class UpdateLeaseRequest(
    val replyTo: ActorRef[StatusReply[UpdateLeaseResponse]],
    val nodesToRenew: List[NodeValue],
    val nodesToReturn: List[NodeValue],
    val newReservationCount: Int
  ) extends Request
  final case class EndLeaseRequest(
    val replyTo: ActorRef[StatusReply[EndLeaseResponse]],
    val nodesToReturn: List[NodeValue]
  ) extends Request
  final case class AckTransactionRequest(
    val transactionId: Int,
    val authorityId: String
  ) extends Request
  
  sealed trait Response 
  final case class DescribeOriginResponse(
    variants: List[SequenceVariant]
  ) extends Response

  // Use an id value handshake rather than a Promise completion as the 
  // latter does not translate to remoting and we eventually want to
  // augment and/or replace a local ZooKeeper authority on each node with
  // a cluster-wide singleton actor that will require Actor remoting.
  // val ackReply: Promise[Unit],
  final case class BeginLeaseResponse(
    val transactionId: Int,
    val reserved: List[NodeReservation]
  ) extends Response

  final case class UpdateLeaseResponse(
    val transactionId: Int,
    val reserved: List[NodeReservation],
    val renewed: List[NodeReservation],
    val replaced: List[NodeReplacement]
  ) extends Response

  final case class EndLeaseResponse(val transactionId: Int) extends Response
  
}

trait OriginBaseState {
  
}

abstract class ULIDRandomBitsOrigin[State <: ULIDRandomBitsOrigin[State]#BaseState] {
  import ULIDRandomBitsOrigin._
  
  abstract class BaseState {
    val activeSequence: Option[SequenceVariant]
//    variants: Optional[Map[String, SequenceVariant]]
  }
  
  def initialize(): Unit = {
    
  }

  def become( behaveFn: (State) => List[PartialFunction[Message, Behavior[Message]]]) = {
    this
  }
}

trait BootstrappedOrigin[State <: BootstrappedOrigin[State]#BootstrapState]
extends ULIDRandomBitsOrigin[State] {
  trait BootstrapState extends BaseState {
  }
}
//trait ULIDRandomBitsAuthority[State] {
//  import ULIDNodeAuthorityProtocol._
//  
//  def initial(state: State): Behavior[Message] = {
//    Behaviors.same;
//  }
//}
//class ULIDRandomBitsAuthority {
//  
//}
