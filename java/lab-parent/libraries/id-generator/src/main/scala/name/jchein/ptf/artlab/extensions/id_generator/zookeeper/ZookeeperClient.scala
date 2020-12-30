package name.jchein.ptf.artlab.extensions.id_generator.zookeeper

import java.time.Duration
import org.apache.curator.framework.CuratorFramework
import scala.collection.JavaConverters._
import scala.concurrent._
import scala.concurrent.duration._
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import org.apache.curator.x.async.AsyncCuratorFramework
import org.apache.curator.framework.recipes.leader.LeaderSelector
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener
import akka.actor.typed.ActorRef.ActorRefOps
import name.jchein.ptf.artlab.extensions.id_generator.IdGeneratorSettings
import org.apache.curator.framework.state.ConnectionStateListener
import org.apache.curator.framework.state.ConnectionState
import org.reactivestreams.Subscriber
import akka.pattern.StatusReply
import org.apache.curator.x.async.modeled.ZNode
import java.time.Instant
import akka.actor.typed.scaladsl.ActorContext


object ZookeeperEvents {
  sealed trait Event
  sealed trait GeneratorIdEvent extends Event {
    val subscriber: ActorRef[Event]
  } 
  sealed trait ZkSessionEvent extends Event

  /**
   * One of three application level events, this indicates a claim is now active over this subscription and its
   * ID is immediatley available for use.
   */
  final case class ClaimedGeneratorIdEvent(
      val subscriber: ActorRef[Event], val generatorId: Long,
      val expiresAfter: Instant, val isRenewal: Boolean) extends GeneratorIdEvent
  
  /**
   * Second of three application level events, this indicates that claim held by this subscription is not longer
   * valid and the subscription channel closes immediately after.
   */
  final case class TerminatedClaimEvent(
      val subscriber: ActorRef[Event], val generatorId: Long,
      val expired: Boolean) extends GeneratorIdEvent
  
  /**
   * Third and final application level even, this indicates that no claim was every allocated and the request has
   * been dropped unfulfilled.  As with termination of a previously fulfilled claim request, the channel 
   * this message arrived through is closed immediately after this message has been sent.
   */
  final case class DroppedRequestEvent(val subscriber: ActorRef[Event]) extends GeneratorIdEvent

  final case class FailedToConnectEvent() extends ZkSessionEvent
  final case class SessionConnectedEvent() extends ZkSessionEvent
  final case class SessionLostEvent() extends ZkSessionEvent
  final case class SessionSuspendedEvent() extends ZkSessionEvent
  final case class SessionResumedEvent() extends ZkSessionEvent
}

object ZookeeperClient {
  sealed trait Request
  sealed trait AskedRequest extends Request {
    val replyTo: ActorRef[StatusReply[Response]]
    val subscriber: ActorRef[_]
  }
  final case class SubscribeGeneratorClientRequest(
    replyTo: ActorRef[StatusReply[Response]], subscriber: ActorRef[ZookeeperEvents.Event]
  ) extends AskedRequest
  final case class UnsubscribeGeneratorClientRequest(
    replyTo: ActorRef[StatusReply[Response]], subscriber: ActorRef[ZookeeperEvents.Event]
  ) extends AskedRequest
  final case class ReleaseGeneratorIdClaimRequest(
    replyTo: ActorRef[StatusReply[Response]], subscriber: ActorRef[ZookeeperEvents.Event]

  ) extends AskedRequest
  
  sealed trait Response
  final case class GeneratorClientSubscribedResponse() extends Response
  final case class GeneratorClientUnsubscribedResponse() extends Response
  

  def apply(
    context: ActorContext[ZookeeperClient.Request], settings: IdGeneratorSettings
  ): Behavior[ZookeeperClient.Request] = {
    context.log.info("Starting Zookeeper Client Actor from Context")
    new ZookeeperClientImpl(context, settings)
  }
}


object ZkInternalProtocol {
  private[zookeeper] sealed trait Internal extends ZookeeperClient.Request
  // Used to direct the client to effect changes, consolidating where changes
  // are applied and and how decisions are made.  Internal Commands will generally
  // follow a fire-and-forget use style, rather than requiring call-response 
  // "ask" semantics.
  private[zookeeper] sealed trait InternalDirective extends Internal
  private[zookeeper] final case class Terminate() extends InternalDirective
  @Deprecated
  private[zookeeper] final case class CheckPendingClaims() extends InternalDirective
  @Deprecated
  private[zookeeper] final case class CheckReleasingClaims() extends InternalDirective
  private[zookeeper] final case class RunClaimMaintenance() extends InternalDirective
  private[zookeeper] final case class AcquireLeaderPriviledge() extends InternalDirective
  private[zookeeper] final case class ReleaseLeaderPriviledge() extends InternalDirective
  private[zookeeper] final case class RegisterWithGeneratorGroup() extends InternalDirective
  private[zookeeper] final case class RenewGeneratorIdClaim() extends InternalDirective

  private[zookeeper] sealed trait InternalTimer extends InternalEvent;
  private[zookeeper] final case class ZkConnectionPastDeadline() extends InternalTimer

  // Remaining subtypes group famies of observations and events.  Most
  // will be generated outside of the Actor thread's. 
  private[zookeeper] sealed trait InternalEvent extends Internal
  private[zookeeper] sealed trait InternalStatus extends InternalEvent
  private[zookeeper] sealed trait InternalLeadership extends InternalEvent
  private[zookeeper] sealed trait InternalWatch extends InternalEvent {
    val event: WatchedEventMeta
  }
  //  private[zookeeper] final case class Subscribe(val subscriber: Subscriber[_ >: ZkClientStreamProtocol.StreamResponse]) extends Internal
  private[zookeeper] final case class ZkConnectionSuccessful() extends InternalStatus
  private[zookeeper] final case class ZkConnectionLost() extends InternalStatus
  private[zookeeper] final case class ZkConnectionSuspended() extends InternalStatus
  private[zookeeper] final case class ZkConnectionReconnected() extends InternalStatus
  private[zookeeper] final case class ZkAcquiredLeadership() extends InternalLeadership
  private[zookeeper] final case class ZkLostLeadership() extends InternalLeadership
  @Deprecated
  private[zookeeper] final case class ZkGenStateLoadFailure(error: Throwable) extends Internal
  @Deprecated
  private[zookeeper] final case class ZkGenStateLoadSuccess(model: ZNode[IdGenConfig]) extends Internal
  private[zookeeper] final case class ZkProcessChildChange(val event: WatchedEventMeta) extends InternalWatch
  private[zookeeper] final case class ZkProcessDataChange(val event: WatchedEventMeta) extends InternalWatch
}


/**
 * Represents a connectivity issue.
 * Thrown if the ZooKeeper client is unable to correct to the server.
 * This error will terminate the instance of the actor.
 */
class ZkClientConnectionFailedException() extends Exception()

/**
 * Represents an internal ZooKeeper client error.
 * Such errors are not recoverable as they indicate state corruption.
 * @param message Human readable description of the problem.
 */
class ZkClientInvalidStateException(val message: String) extends Exception(message)
