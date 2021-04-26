package name.jchein.ptf.artlab.extensions.id_generator.zookeeper

import java.time.Duration

import org.apache.curator.x.async.modeled.versioned.Versioned

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.SupervisorStrategy
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.StashBuffer
import name.jchein.ptf.artlab.extensions.id_generator.IdGeneratorSettings
import name.jchein.ptf.artlab.extensions.id_generator.ZookeeperULIDAuthority
import name.jchein.ptf.artlab.extensions.zookeeper.ZookeeperClient
import name.jchein.ptf.artlab.extensions.zookeeper.ZookeeperLeaderRecipe
import name.jchein.ptf.artlab.extensions.zookeeper.ZookeeperModelNode
import name.jchein.ptf.artlab.extensions.id_generator.ZookeeperULIDAuthority

case class ZookeeperULIDAuthorityState()

/**
 * Protocol for using Zookeeper to manage the initializing the "node" region
 * of a ZookeeperULIDStrategy based on a three-tier decomposition:
 * -- Node : A region for a byte sequence that is unique across all ULIDFactories in use at any
 *           point in time.  ZooKeeper is used to provide a means of distributing unique identifiers
 *           that servethis purpose
 * -- Epoch : A region that is incremented whenever the clock moved backwards or the
 *            series region overflows within a single clock tick.
 * -- Series : A region that is incremneted whenever the clock's tick value is the same between two
 *             consecutive ULID creations.
 */
object ZookeeperULIDAuthority extends ZookeeperULIDAuthority {
  sealed trait Message
  
  private[zookeeper] sealed trait InternalEvent
  // Discovery-related internal messages
  private[zookeeper] final case class WrappedListing(
    val event: Receptionist.Listing
  ) extends InternalEvent
  private[zookeeper] final case class LeaderLatchActivation(
    val serviceKey: ServiceKey[ZookeeperLeaderRecipe.Message]
  ) extends InternalEvent 
  private[zookeeper] final case class ConfigModelActivation(
    val serviceKey: ServiceKey[ZookeeperModelNode.Message[VariantConfigModel]]
  ) extends InternalEvent 
  private[zookeeper] final case class SequenceModelActivation(
    val serviceKey: ServiceKey[ZookeeperModelNode.Message[NodeSequenceModel]]
  ) extends InternalEvent 
  private[zookeeper] final case class RegisterModelActivation(
    val serviceKey: ServiceKey[ZookeeperModelNode.Message[LeaseRegistrationModel]]
  ) extends InternalEvent 
  private[zookeeper] final case class ZkConnectionSuccessful() extends InternalEvent
  private[zookeeper] final case class ZkConnectionLost() extends InternalEvent
  private[zookeeper] final case class ZkConnectionSuspended() extends InternalEvent
  private[zookeeper] final case class ZkConnectionReconnected() extends InternalEvent

//  sealed trait Event extends Message
//
//  /**
//   * These messages are derived by MessageAdapter from their more generic similars from
//   * ZookeeperClient.WatchClientEvent.  Their origin can be found in the ZooKeeper watch
//   * mixin classes as reused here.
//   */
//  sealed trait WatchClientEvent extends Event
//  final case class AcquiredLeaderRole() extends WatchClientEvent
//  final case class LostLeaderRole() extends WatchClientEvent
//  final case class ReceivedSequenceConfigUpdate(meta: WatchedEventMeta, val model: ZNode[NodeSequenceModel]) extends WatchClientEvent
//  final case class ReceivedSequencePositionUpdate(meta: WatchedEventMeta, val model: ZNode[VariantConfigModel]) extends WatchClientEvent
//  final case class ReceivedRegisteredClaimUpdate(meta: WatchedEventMeta) extends WatchClientEvent
//  
//  /**
//   * Not sure if this is where these belong yet.  I think these will be understood better
//   * through their use cases, which should involve AsyncStage return values created by Curator.
//   */
//  final case class SequencePositionWriteFailed(meta: WatchedEventMeta) extends WatchClientEvent
//  final case class SequencePositionWriteSucceeded(meta: WatchedEventMeta) extends WatchClientEvent
//  final case class AcquiredLeaseCreateFailed(meta: WatchedEventMeta) extends WatchClientEvent
//  final case class AcquiredLeaseCreateSucceeded(meta: WatchedEventMeta) extends WatchClientEvent
//  
//  sealed trait ZkSessionEvent extends Event
//  final case class SessionConnectedEvent() extends ZkSessionEvent
//  final case class SessionLostEvent() extends ZkSessionEvent
//  final case class SessionSuspendedEvent() extends ZkSessionEvent
//  final case class SessionResumedEvent() extends ZkSessionEvent
//
//  /**
//   * Not sure if these need named messages to occur as side effects at the appropriate times
//   */
//  final case class UpdateSequenceConfig(val variantId: Byte, val commandId: Int, srcConfig: IdGeneratorSettings.ZookeeperLeaseConfigSettings) extends Command
//  final case class WatchSequencePosition(val variantId: Byte, val commandId: Int) extends Command
//  final case class UnwatchSequencePosition(val variantId: Byte, val commandId: Int) extends Command
//  final case class UpdateSequencePosition(
//    val variantId: Byte, 
//    val commandId: Int,
//    val updateContent: Versioned[NodeSequenceModel]
//  ) extends Command
//  final case class RecordLeaseAcquisition(
//    val variantId: Byte, val commandId: Int, val nodeId: Long, 
//    val renewAfter: Instant, val expireAfter: Instant ) extends Command
// 
//  sealed trait GeneratorIdEvent extends Event {
//    val subscriptionId: Int
//  }
//  final case class SubscribedEvent(val subscriptionId: Int) extends GeneratorIdEvent {
//  }
//  final case class UnsubscribedEvent(val subscriptionId: Int) extends GeneratorIdEvent {
//  }
//
//  /**
//   * One of three application level events, this indicates a claim is now active over this subscription and its
//   * ID is immediatley available for use.
//   */
//  final case class AcquiredSourceNodeClaimEvent(
//    val subscriptionId: Int,
//    val sourceNodeId:   Long,
//    val renewsAfter:    Instant,
//    val expiresAfter:   Instant,
//    val isRenewal:      Boolean
//  ) extends GeneratorIdEvent
//
//  /**
//   * Second of three application level events, this indicates that claim held by this subscription is not longer
//   * valid and the subscription channel closes immediately after.
//   */
//  final case class TerminatedSourceNodeClaimEvent(
////    val subscriber:   ActorRef[Event],
//    val subscriptionId: Int,
//    val sourceNodeId: Long,
//    val renewsAfter:  Instant,
//    val expiresAfter: Instant,
//    val expired:      Boolean
//  ) extends GeneratorIdEvent
//
//  /**
//   * This indicates that a claim has been abandoned before it was ever fulfilled, and so there is no associated
//   * termination, and this event implies there never will be anything to terminate.
//   * been dropped unfulfilled.  As with termination of a previously fulfilled claim request, the channel
//   * this message arrived through is closed immediately after this message has been sent.
//   */
//  final case class AbandonedUnsatisfiedRequestEvent(val subscriptionId: Int) extends GeneratorIdEvent
////    val subscriber: ActorRef[Event]) extends GeneratorIdEvent
 
  def apply(
    settings: ZookeeperULIDAuthorityExtension.Settings
  ): Behavior[ZookeeperULIDAuthority.Message] = {
    Behaviors.supervise(
      Behaviors.setup[ZookeeperULIDAuthority.Message](
        (context: ActorContext[ZookeeperULIDAuthority.Message]) ⇒ {
          
//          val sessionEventAdapter: ActorRef[ZookeeperSessionWatcher.Event] =
//        	  context.messageAdapter((src: ZookeeperSessionWatcher.Event) =>
//        	    src match {
//        	      case evt: ZookeeperSessionWatcher.ZkConnectionSuccessful => 
//        	        ZookeeperULIDAuthority.ZkConnectionSuccessful()
//        	      case evt: ZookeeperSessionWatcher.ZkConnectionLost => 
//        	        ZookeeperULIDAuthority.ZkConnectionLost()
//        	      case evt: ZookeeperSessionWatcher.ZkConnectionSuspended => 
//        	        ZookeeperULIDAuthority.ZkConnectionSuspended()
//        	      case evt: ZookeeperSessionWatcher.ZkConnectionReconnected => 
//        	        ZookeeperULIDAuthority.ZkConnectionReconnected()
//        	    }
//        	  )

          // Get Keys for the leader and three model actors and subscribe 
          // for them as well
//          new ZookeeperULIDAuthority(context, settings).activate()
          // new ZookeeperULIDAuthorityFactory(context, settings)
          Behaviors.empty
        }
      )
    ).onFailure[Throwable](
        SupervisorStrategy.restartWithBackoff(
          Duration.ofMillis(250), Duration.ofSeconds(45), 0.2
        )
      )
  }
  
  private[zookeeper] sealed trait State
  private[zookeeper] final case class AppState(
    val configModel: VariantConfigModel,
    val sequenceModel: Versioned[NodeSequenceModel],
    val registrations: Array[LeaseRegistrationModel]
  ) extends State

  def becomeNext[T](
    currentState: T, nextBehavior: (T) ⇒ Behavior[ZookeeperULIDAuthority.Message]
  ): Behavior[ZookeeperULIDAuthority.Message] = {
    return nextBehavior.apply(currentState)
  }
}

class ZookeeperULIDAuthority(
  val context:   ActorContext[ZookeeperULIDAuthority.Message],
  val settings: ZookeeperULIDAuthorityExtension.Settings,
  val stash: StashBuffer[ZookeeperULIDAuthority.Message],
  val zkSession: ActorRef[ZkClient.Message],
  val zkLeader: ActorRef[ZookeeperLeaderRecipe.Message],
  val zkConfigCache: ActorRef[ZookeeperModelNode.Message[VariantConfigModel]],
  val zkSeriesNode: ActorRef[ZookeeperModelNode.Message[NodeSequenceModel]],
  val zkRootChildNode: ActorRef[ZookeeperModelNode.Message[LeaseRegistrationModel]],
) {

//	private[zookeeper] object IdReservationPhase extends Enumeration {
//		type IdReservationPhase = Value;
//
//		val INITIAL_PROPOSAL = Value("AtomicProposal");
//		val RESOLVING_CONFLICT  = Value("ResolvingConflict");
//		val GROUP_ENROLLMENT = Value("GroupEnrollment");
//		val WAITING_TO_ABORT = Value("WaitingToAbort");
//	}
//
//	private[zookeeper] sealed trait GeneratorIdClaimState {
//		val subscriber: ActorRef[ZookeeperULIDAuthority.Event];
//	}
//
//	private[zookeeper] case class RequestingGeneratorIdClaimState(
//		val subscriber: ActorRef[ZookeeperULIDAuthority.Event],
//	) extends GeneratorIdClaimState
//
//	private[zookeeper] case class ProposedGeneratorIdClaimState(
//		val subscriber: ActorRef[ZookeeperULIDAuthority.Event],
//		val generatorId: Long,
//		val expiresAfter: Instant,
//	) extends GeneratorIdClaimState
//
//	private[zookeeper] case class JoiningGeneratorIdClaimState(
//		val subscriber: ActorRef[ZookeeperULIDAuthority.Event],
//		val generatorId: Long,
//		val expiresAfter: Instant,
//	) extends GeneratorIdClaimState
//
//	private[zookeeper] case class CurrentGeneratorIdClaimState(
//		val subscriber: ActorRef[ZookeeperULIDAuthority.Event],
//		val generatorId: Long, expiresAfter: Instant,
//		val groupMember: GroupMember
//	) extends GeneratorIdClaimState
//
//	private[zookeeper] case class StaleGeneratorIdClaimState(
//		val subscriber: ActorRef[ZookeeperULIDAuthority.Event],
//		val generatorId: Long, expiresAfter: Instant,
//		val groupMember: GroupMember
//	) extends GeneratorIdClaimState
//
//	private[zookeeper] case class RenewingGeneratorIdClaimState(
//		val subscriber: ActorRef[ZookeeperULIDAuthority.Event],
//		val generatorId: Long, expiresAfter: Instant,
//		val groupMember: GroupMember
//	) extends GeneratorIdClaimState
//
//	private[zookeeper] case class QuiescingGeneratorIdClaimState(
//		val subscriber: ActorRef[ZookeeperULIDAuthority.Event],
//		val generatorId: Long, expiresAfter: Instant,
//		val groupMember: Optional[GroupMember]
//	) extends GeneratorIdClaimState
//
//	private[zookeeper] case class ReleasingGeneratorIdClaimState(
//		val subscriber: ActorRef[ZookeeperULIDAuthority.Event],
//		val generatorId: Long
//	) extends GeneratorIdClaimState

	//  private[zookeeper] case class ClosedGeneratorIdClaimState(
	//  subscriber: ActorRef[ZookeeperULIDAuthority.Event],
	//  generatorId: Long,
	//  ) extends GeneratorIdClaimState

//	private[zookeeper] case class State(
//		val curatorClient: AsyncCuratorFramework,
//		val nodeSequenceModelNode: ModeledFramework[NodeSequenceModel],
//		val variantConfigModelNode: ModeledFramework[VariantConfigModel],
//		val versionedModelNode: VersionedModeledFramework[NodeSequenceModel],
//		val genClaimsSeq: PSequence[GeneratorIdClaimState] = TreePVector.empty(),
//		val genClaimsByActorRef: PMap[ActorRef[ZookeeperULIDAuthority.Event], GeneratorIdClaimState] = HashTreePMap.empty(),
//		val genClaimsById: PMap[Long, GeneratorIdClaimState] = HashTreePMap.empty(),
//		val leaderLatch: Optional[LeaderLatch] = Optional.empty(),
//		val idGenConfigWatch: Optional[CuratorWatcher] = Optional.empty(),
//		val idGenConfigModel: Optional[Versioned[NodeSequenceModel]] = Optional.empty(),
//		val pendingIdClaimProposal: Optional[AsyncResult[NodeSequenceModel]] = Optional.empty(),
//		val pendingProposalNewIds: PSequence[Long] = TreePVector.empty()
//	) {
//		
//		def hasRequestedClaims(): Boolean = 
//			genClaimsSeq.stream.anyMatch { claimState => 
//			  claimState.isInstanceOf[RequestingGeneratorIdClaimState] }
//
//		def hasProposedClaims(): Boolean = 
//			genClaimsSeq.stream.anyMatch { claimState => 
//			  claimState.isInstanceOf[ProposedGeneratorIdClaimState] }
//
//		def hasJoiningClaims(): Boolean = 
//			genClaimsSeq.stream.anyMatch { claimState => 
//			 	claimState.isInstanceOf[JoiningGeneratorIdClaimState] }
//
//		def hasCurrentClaims(): Boolean = 
//			genClaimsSeq.stream.anyMatch { claimState => 
//				claimState.isInstanceOf[CurrentGeneratorIdClaimState] }
//
//		def hasRenewingClaims(): Boolean = 
//			genClaimsSeq.stream.anyMatch { claimState => 
//			  claimState.isInstanceOf[RenewingGeneratorIdClaimState] }
//
//		def hasQuiescingClaims(): Boolean = 
//			genClaimsSeq.stream.anyMatch { claimState => 
//			  claimState.isInstanceOf[QuiescingGeneratorIdClaimState] }
//
//		def hasReleasingClaims(): Boolean = 
//			genClaimsSeq.stream.anyMatch { claimState => 
//		  	claimState.isInstanceOf[ReleasingGeneratorIdClaimState] }
//	}

  def activate(): Behavior[ZookeeperULIDAuthority.Message] = {
    Behaviors.same
  }
}

//
//object ZkInternalProtocol {
//  private[zookeeper] sealed trait Internal extends ZookeeperULIDAuthority.Request
//  // Used to direct the client to effect changes, consolidating where changes
//  // are applied and and how decisions are made.  Internal Commands will generally
//  // follow a fire-and-forget use style, rather than requiring call-response
//  // "ask" semantics.
//  private[zookeeper] sealed trait InternalDirective extends Internal
//  private[zookeeper] final case class Terminate() extends InternalDirective
//  @Deprecated
//  private[zookeeper] final case class CheckPendingClaims() extends InternalDirective
//  @Deprecated
//  private[zookeeper] final case class CheckReleasingClaims() extends InternalDirective
//  private[zookeeper] final case class RunClaimMaintenance() extends InternalDirective
//  private[zookeeper] final case class AcquireLeaderPriviledge() extends InternalDirective
//  private[zookeeper] final case class ReleaseLeaderPriviledge() extends InternalDirective
//  private[zookeeper] final case class RegisterWithGeneratorGroup() extends InternalDirective
//  private[zookeeper] final case class RenewGeneratorIdClaim() extends InternalDirective
//
//  private[zookeeper] sealed trait InternalTimer extends InternalEvent;
//  private[zookeeper] final case class ZkConnectionPastDeadline() extends InternalTimer
//
//  // Remaining subtypes group famies of observations and events.  Most
//  // will be generated outside of the Actor thread's.
//  private[zookeeper] sealed trait InternalEvent extends Internal
//  private[zookeeper] sealed trait InternalStatus extends InternalEvent
//  private[zookeeper] sealed trait InternalLeadership extends InternalEvent
//  private[zookeeper] sealed trait InternalWatch extends InternalEvent {
//    val event: WatchedEventMeta
//  }
//  //  private[zookeeper] final case class Subscribe(val subscriber: Subscriber[_ >: ZkClientStreamProtocol.StreamResponse]) extends Internal
//  private[zookeeper] final case class ZkConnectionSuccessful() extends InternalStatus
//  private[zookeeper] final case class ZkConnectionLost() extends InternalStatus
//  private[zookeeper] final case class ZkConnectionSuspended() extends InternalStatus
//  private[zookeeper] final case class ZkConnectionReconnected() extends InternalStatus
//  private[zookeeper] final case class ZkAcquiredLeadership() extends InternalLeadership
//  private[zookeeper] final case class ZkLostLeadership() extends InternalLeadership
//  @Deprecated
//  private[zookeeper] final case class ZkGenStateLoadFailure(error: Throwable) extends Internal
//  @Deprecated
//  private[zookeeper] final case class ZkGenStateLoadSuccess(model: ZNode[NodeSequenceModel]) extends Internal
//  private[zookeeper] final case class ZkProcessChildChange(val event: WatchedEventMeta) extends InternalWatch
//  private[zookeeper] final case class ZkProcessDataChange(val event: WatchedEventMeta) extends InternalWatch
//}
//
