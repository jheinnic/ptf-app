package name.jchein.ptf.artlab.extensions.id_generator.zookeeper

import java.time.Instant
import java.util.Optional

import scala.concurrent.duration.Duration

import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.api.CuratorWatcher
import org.apache.curator.framework.recipes.leader.LeaderLatch
import org.apache.curator.framework.recipes.leader.LeaderLatchListener
import org.apache.curator.framework.recipes.nodes.GroupMember
import org.apache.curator.framework.state.ConnectionState
import org.apache.curator.framework.state.ConnectionStateListener
import org.apache.curator.x.async.AsyncCuratorFramework
import org.apache.curator.x.async.AsyncResult
import org.apache.curator.x.async.modeled.ModeledFramework
import org.apache.curator.x.async.modeled.ZNode
import org.apache.zookeeper.WatchedEvent
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.Watcher.Event.KeeperState
import org.pcollections.HashTreePMap
import org.pcollections.PMap
import org.pcollections.PSequence
import org.pcollections.TreePVector

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.pattern.StatusReply
import name.jchein.ptf.artlab.extensions.id_generator.IdGeneratorSettings
import org.apache.curator.x.async.modeled.versioned.VersionedModeledFramework
import org.apache.curator.x.async.modeled.versioned.Versioned
import java.util.Calendar


class ZookeeperClientImpl(
	context: ActorContext[ZookeeperClient.Request],
	val settings: IdGeneratorSettings,
) extends AbstractBehavior(context) with ZkClientWatcher {
	private[zookeeper] object IdReservationPhase extends Enumeration {
		type IdReservationPhase = Value;

		val INITIAL_PROPOSAL = Value("AtomicProposal");
		val RESOLVING_CONFLICT  = Value("ResolvingConflict");
		val GROUP_ENROLLMENT = Value("GroupEnrollment");
		val WAITING_TO_ABORT = Value("WaitingToAbort");
	}

	private[zookeeper] sealed trait GeneratorIdClaimState {
		val subscriber: ActorRef[ZookeeperEvents.Event];
	}

	private[zookeeper] case class RequestingGeneratorIdClaimState(
		val subscriber: ActorRef[ZookeeperEvents.Event],
	) extends GeneratorIdClaimState

	private[zookeeper] case class ProposedGeneratorIdClaimState(
		val subscriber: ActorRef[ZookeeperEvents.Event],
		val generatorId: Long,
		val expiresAfter: Instant,
	) extends GeneratorIdClaimState

	private[zookeeper] case class JoiningGeneratorIdClaimState(
		val subscriber: ActorRef[ZookeeperEvents.Event],
		val generatorId: Long,
		val expiresAfter: Instant,
	) extends GeneratorIdClaimState

	private[zookeeper] case class CurrentGeneratorIdClaimState(
		val subscriber: ActorRef[ZookeeperEvents.Event],
		val generatorId: Long, expiresAfter: Instant,
		val groupMember: GroupMember
	) extends GeneratorIdClaimState

	private[zookeeper] case class StaleGeneratorIdClaimState(
		val subscriber: ActorRef[ZookeeperEvents.Event],
		val generatorId: Long, expiresAfter: Instant,
		val groupMember: GroupMember
	) extends GeneratorIdClaimState

	private[zookeeper] case class RenewingGeneratorIdClaimState(
		val subscriber: ActorRef[ZookeeperEvents.Event],
		val generatorId: Long, expiresAfter: Instant,
		val groupMember: GroupMember
	) extends GeneratorIdClaimState

	private[zookeeper] case class QuiescingGeneratorIdClaimState(
		val subscriber: ActorRef[ZookeeperEvents.Event],
		val generatorId: Long, expiresAfter: Instant,
		val groupMember: Optional[GroupMember]
	) extends GeneratorIdClaimState

	private[zookeeper] case class ReleasingGeneratorIdClaimState(
		val subscriber: ActorRef[ZookeeperEvents.Event],
		val generatorId: Long
	) extends GeneratorIdClaimState

	//  private[zookeeper] case class ClosedGeneratorIdClaimState(
	//  subscriber: ActorRef[ZookeeperEvents.Event],
	//  generatorId: Long,
	//  ) extends GeneratorIdClaimState

	private[zookeeper] case class State(
		val curatorClient: AsyncCuratorFramework,
		val genIdModelClient: ModeledFramework[IdGenConfig],
		val versionedModelClient: VersionedModeledFramework[IdGenConfig],
		val genClaimsSeq: PSequence[GeneratorIdClaimState] = TreePVector.empty(),
		val genClaimsByActorRef: PMap[ActorRef[ZookeeperEvents.Event], GeneratorIdClaimState] = HashTreePMap.empty(),
		val genClaimsById: PMap[Long, GeneratorIdClaimState] = HashTreePMap.empty(),
		val leaderLatch: Optional[LeaderLatch] = Optional.empty(),
		val idGenConfigWatch: Optional[CuratorWatcher] = Optional.empty(),
		val idGenConfigModel: Optional[Versioned[IdGenConfig]] = Optional.empty(),
		val pendingIdClaimProposal: Optional[AsyncResult[IdGenConfig]] = Optional.empty(),
		val pendingProposalNewIds: PSequence[Long] = TreePVector.empty()
	) {
		
		def hasRequestedClaims(): Boolean = 
			genClaimsSeq.stream.anyMatch { claimState => 
			  claimState.isInstanceOf[RequestingGeneratorIdClaimState] }

		def hasProposedClaims(): Boolean = 
			genClaimsSeq.stream.anyMatch { claimState => 
			  claimState.isInstanceOf[ProposedGeneratorIdClaimState] }

		def hasJoiningClaims(): Boolean = 
			genClaimsSeq.stream.anyMatch { claimState => 
			 	claimState.isInstanceOf[JoiningGeneratorIdClaimState] }

		def hasCurrentClaims(): Boolean = 
			genClaimsSeq.stream.anyMatch { claimState => 
				claimState.isInstanceOf[CurrentGeneratorIdClaimState] }

		def hasRenewingClaims(): Boolean = 
			genClaimsSeq.stream.anyMatch { claimState => 
			  claimState.isInstanceOf[RenewingGeneratorIdClaimState] }

		def hasQuiescingClaims(): Boolean = 
			genClaimsSeq.stream.anyMatch { claimState => 
			  claimState.isInstanceOf[QuiescingGeneratorIdClaimState] }

		def hasReleasingClaims(): Boolean = 
			genClaimsSeq.stream.anyMatch { claimState => 
		  	claimState.isInstanceOf[ReleasingGeneratorIdClaimState] }
	}

	private[zookeeper] val claimSubscriberBehavior:
	  PartialFunction[ZookeeperClient.Request, Behavior[ZookeeperClient.Request]] = { 
	  case ZookeeperClient.SubscribeGeneratorClientRequest(
			replyTo: ActorRef[StatusReply[ZookeeperClient.Response]],
			subscriber: ActorRef[ZookeeperEvents.Event]
		) =>
	    context.log.info("In claimSubsriberBehavior() subscription case")
     	context.log.info("Still in claimSubsriberBehavior() subscription")
     	replyTo ! StatusReply.success(ZookeeperClient.GeneratorClientSubscribedResponse())
     	context.log.info("Sent reply!")
     	context.self ! ZkInternalProtocol.RunClaimMaintenance()
     	val newRequest = new RequestingGeneratorIdClaimState(subscriber)
     	changeState(
     		currentState.copy(
     			genClaimsSeq = currentState.genClaimsSeq.plus(newRequest),
     			genClaimsByActorRef = currentState.genClaimsByActorRef.plus(subscriber, newRequest)
     		)
     	)
	  case ZookeeperClient.UnsubscribeGeneratorClientRequest(
			replyTo: ActorRef[StatusReply[ZookeeperClient.Response]],
			subscriber: ActorRef[ZookeeperEvents.Event]
		) =>
	    val toRemove = currentState.genClaimsByActorRef.get(subscriber)
	    toRemove match {
	      case null =>
	        replyTo ! StatusReply.error("No such subscriber to unsubscribe")
	        Behaviors.unhandled
	      case active: CurrentGeneratorIdClaimState =>
	        replyTo ! StatusReply.error(
			      new IllegalStateException("Release generator ID claim before canceling subscription")
			    )
	        Behaviors.unhandled
	      case request: RequestingGeneratorIdClaimState =>
	        subscriber ! new ZookeeperEvents.DroppedRequestEvent(subscriber)
	        replyTo ! StatusReply.success(ZookeeperClient.GeneratorClientUnsubscribedResponse())
	        changeState(
			      currentState.copy(
					    genClaimsSeq = currentState.genClaimsSeq.minus(toRemove),
					    genClaimsByActorRef = currentState.genClaimsByActorRef.minus(subscriber)
					  )
			    )
	      case request: ProposedGeneratorIdClaimState =>
	        subscriber ! new ZookeeperEvents.DroppedRequestEvent(subscriber)
	        replyTo ! StatusReply.success(ZookeeperClient.GeneratorClientUnsubscribedResponse())
	        changeState(
			      currentState.copy(
					    genClaimsSeq = currentState.genClaimsSeq.minus(toRemove),
					    genClaimsByActorRef = currentState.genClaimsByActorRef.minus(subscriber)
					  )
			    )
	    }
	}

	def failed(): Behavior[ZookeeperClient.Request] = { 
	  context.log.info("In failed()")
		Behaviors.receiveMessage(
		  // claimSubscriberBehavior orElse {
		  {
				case src: ZookeeperClient.AskedRequest =>
				  src.replyTo ! StatusReply.error(s"Received {_.toString()} after failure")
				  context.log.error(s"Received {_.toString()} after failure")
				  Behaviors.same
				case _ => 
				  context.log.error(s"Received {_.toString()} after failure")
				  Behaviors.same
	    }
		)
	}

	def disconnected(): Behavior[ZookeeperClient.Request] = {
		context.log.info("In disconnected()")
		Behaviors.withStash(1000) { stash => 
			Behaviors.receiveMessage(
				claimSubscriberBehavior orElse {
					case ZkInternalProtocol.ZkConnectionSuccessful() =>
					  publishEvent(ZookeeperEvents.SessionConnectedEvent())
					  stash.unstashAll(
							changeBehavior(connected)
						)
					case ZkInternalProtocol.ZkConnectionPastDeadline() =>
					  publishEvent(new ZookeeperEvents.FailedToConnectEvent())
					  stash.unstashAll(
							changeBehavior(failed)
						)
					case ZkInternalProtocol.ZkConnectionLost() =>
					  context.log.warn("Received redundant connection loss event while already disconnected")
					  Behaviors.same
					case ZkInternalProtocol.ZkConnectionSuspended() =>
					  context.log.error("Received absurd connection suspended event while already disconnected")
					  Behaviors.unhandled
					case ZkInternalProtocol.ZkConnectionReconnected() =>
					  context.log.error("Received absurd connection resumed event while already disconnected")
					  Behaviors.unhandled
					case msg: ZkInternalProtocol.RunClaimMaintenance =>
					  stash.stash(msg)
					  Behaviors.same
					case _ => 
					  context.log.error(s"Unexpected message, {_.toString()}, received while client session is disconnected.")
					  Behaviors.unhandled
					}
				)
			}
	}

	val connected: Behavior[ZookeeperClient.Request] = 
		Behaviors.receiveMessage(
		  claimSubscriberBehavior orElse {
				case ZkInternalProtocol.RunClaimMaintenance() => 
					val myId: String = context.system.address.toString
					val latch = new LeaderLatch(
							currentState.curatorClient.unwrap(), settings.ZK_ZNODE, myId)
					latch.addListener(leadershipListener)
					changeState(
						currentState.copy(
							leaderLatch = Optional.of(latch)
						)
					)
				case ZkInternalProtocol.ZkAcquiredLeadership() =>
					if (currentState.hasProposedClaims()) {
						AsyncResult.of(
								currentState.genIdModelClient.readAsZNode()
								).thenAccept { znode =>
								if (znode.getException.isPresent) {
									context.self ! new ZkInternalProtocol.ZkGenStateLoadFailure(znode.getException.get)
								}
								currentState.genIdModelClient.set(
										znode.getValue.get.model, znode.getRawValue.stat.getVersion)
						}
					}
					Behaviors.same
				case ZkInternalProtocol.ZkGenStateLoadSuccess(znode: ZNode[IdGenConfig]) =>
					Behaviors.same
				case ZkInternalProtocol.ZkGenStateLoadFailure(throwable: Throwable) =>
					Behaviors.same
//				case ZkInternalProtocol.ZkAcquiredLeadership() =>
//					Behaviors.same
				case ZkInternalProtocol.ZkConnectionSuccessful() =>
					publishEvent(ZookeeperEvents.SessionConnectedEvent())
					changeBehavior(connected)
				case ZkInternalProtocol.ZkConnectionLost()  =>
					publishEvent(ZookeeperEvents.SessionLostEvent())
					changeBehavior(disconnected)
				case ZkInternalProtocol.ZkConnectionPastDeadline() =>
					publishEvent(ZookeeperEvents.FailedToConnectEvent())
					changeBehavior(failed)
				case ZkInternalProtocol.ZkProcessChildChange(event: WatchedEventMeta)  =>
					Behaviors.same
				case ZkInternalProtocol.ZkProcessDataChange(event: WatchedEventMeta)  =>
					Behaviors.same
			}
		)
			//      context.log.info(s"Zookeeper bootstrap pending on {Thread.currentThread()}")
			//      handle(state)
			//              handle(new BootstrappingState(settings, client)) // closeableServices))
			//        case ZookeeperClient.claimGenIdReq: ClaimGeneratorIdRequest =>
			//              Behaviors.same
			//                val myId = System.getenv("HOSTNAME")
			//                val group = new GroupMember(client, settings.ZK_ZNODE, myId, new Array[Byte](3))
			//                var seedEntryAdded = false
			//                val closeableServices: PSet[Closeable] = HashTreePSet.singleton(group)

	context.log.info("Creating Zookeeper Client from thread {Thread.currentThread()}")

	val curatorClient = AkkaCuratorClient(settings)
	val modelClient = IdGenModelClient(curatorClient, settings)
	val versionedClient = modelClient.versioned()
	var currentState: State = State(curatorClient, modelClient, versionedClient)
	var currentBehavior: Behavior[ZookeeperClient.Request] = disconnected

	val connectStateListener = new ConnectionStateListener {
		override def stateChanged(client: CuratorFramework, newState: ConnectionState): Unit = 
			newState match {
				case ConnectionState.CONNECTED =>
				  context.self ! ZkInternalProtocol.ZkConnectionSuccessful()
				case ConnectionState.LOST =>
				  context.self ! ZkInternalProtocol.ZkConnectionLost()
				case ConnectionState.READ_ONLY =>
				  context.log.info("Read only")
				case ConnectionState.RECONNECTED =>
				  context.self ! ZkInternalProtocol.ZkConnectionReconnected()
				case ConnectionState.SUSPENDED =>
				  context.self ! ZkInternalProtocol.ZkConnectionSuspended()
		}
	}
	val leadershipListener = new LeaderLatchListener() {
		override def isLeader: Unit = {
			context.log.info(s"component=idgen-zookeeper-client at=leader-change status=Became a leader")
			context.self ! ZkInternalProtocol.ZkAcquiredLeadership()
	  }

	  override def notLeader(): Unit = {
			context.log.info(s"component=idgen-zookeeper-client at=leader-change status=Lost leadership")
			context.self ! ZkInternalProtocol.ZkLostLeadership()
	  }
	}

	currentState.curatorClient
		.unwrap()
		.getConnectionStateListenable()
		.addListener(connectStateListener)

	def changeState(nextState: State, nextBehavior: Behavior[ZookeeperClient.Request] = currentBehavior): Behavior[ZookeeperClient.Request] = {
		currentState = nextState
		currentBehavior = nextBehavior
		currentBehavior
	}

	def changeBehavior(nextBehavior: Behavior[ZookeeperClient.Request]): Behavior[ZookeeperClient.Request] = {
    currentBehavior = nextBehavior
	  currentBehavior
	}

//	def publishEvent(nextEvent: ZookeeperEvents.Event): Unit =
//		nextEvent match {
//			case genIdEvent: ZookeeperEvents.GeneratorIdEvent =>
//				genIdEvent.subscriber ! genIdEvent
//	  	case clientEvent: ZookeeperEvents.ZkSessionEvent =>
//				currentState.genClaimsSeq.forEach { claimState => 
//				  claimState.subscriber ! clientEvent
//		    }
//	  }

	def publishEvent(nextEvent: ZookeeperEvents.ZkSessionEvent): Unit =
		currentState.genClaimsSeq.forEach { claimState => 
			claimState.subscriber ! nextEvent
	  }

	override def onMessage(msg: ZookeeperClient.Request): Behavior[ZookeeperClient.Request] = {
		context.log.info(
			String.format("Called onMessage(%s), returning %s", msg, claimSubscriberBehavior)
		)
		claimSubscriberBehavior.apply(msg)
	}
}


/**
 * ZooKeeper watcher mixin.
 *
 * Contains the logic for handling ZooKeeper [[org.apache.zookeeper.WatchedEvent]]
 */
trait ZkClientWatcher extends Watcher { this: ZookeeperClientImpl =>
  private var currentState = KeeperState.Disconnected

	/**
	 * Process an incoming [[org.apache.zookeeper.WatchedEvent]].
	 * @param event event to process
	 */
	override def process(event: WatchedEvent): Unit = {
		val meta = WatchedEventMeta(event)

		if (currentState != event.getState && event.getState == KeeperState.SyncConnected) {
				this.context.self ! ZkInternalProtocol.ZkConnectionSuccessful()
		}

		if (currentState != event.getState && event.getState == KeeperState.Disconnected) {
			  this.context.self ! ZkInternalProtocol.ZkConnectionLost()
		}

		currentState = event.getState

		if (meta.stateChanged) {
				context.log.info("Received Connection State MetaEvent, but the listener should have or should soon process same")
		}

		if (meta.dataChanged) {
		  	this.context.self ! ZkInternalProtocol.ZkProcessDataChange(meta)
		}

		if (meta.childrenChanged) {
		  	this.context.self ! ZkInternalProtocol.ZkProcessChildChange(meta)
		}
}
}

