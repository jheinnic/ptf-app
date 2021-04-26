package name.jchein.ptf.artlab.extensions.zookeeper

import java.util.Optional

import scala.collection.mutable.HashMap

import org.apache.curator.x.async.AsyncCuratorFramework
import org.apache.curator.x.async.modeled.ModelSpec
import org.apache.curator.x.async.modeled.ZPath

import akka.pattern.StatusReply
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.StashBuffer


object ZkClient {
  sealed trait Message

  sealed trait Request[Rsp <: Response] extends Message {
    val replyTo: ActorRef[StatusReply[Rsp]]
  }

  final case class LeaderRecipeRequest(
    val zpath: ZPath,
    val replyTo: ActorRef[StatusReply[LeaderRecipeResponse]]
  ) extends Request[LeaderRecipeResponse]
  final case class ModelNodeRequest[Mdl](
    val zpath: ZPath,
    val modelSpec: ModelSpec[Mdl],
    val replyTo: ActorRef[StatusReply[ModelNodeResponse[Mdl]]]
  ) extends Message

  sealed trait Response
  final case class LeaderRecipeResponse(
    val serviceKey: ServiceKey[ZookeeperLeaderRecipe.Message]
  ) extends Response
  final case class ModelNodeResponse[T](
    val serviceKey: ServiceKey[ZookeeperModelNode.Message[T]]
  ) extends Response

  private[zookeeper] sealed trait InternalEvent extends Message
  private[zookeeper] final case class ZkConnectionSuccessful() extends InternalEvent
  private[zookeeper] final case class ZkConnectionLost() extends InternalEvent
  private[zookeeper] final case class ZkConnectionSuspended() extends InternalEvent
  private[zookeeper] final case class ZkConnectionReconnected() extends InternalEvent

  val Key: ServiceKey[ZkClient.Message] = ServiceKey(Constants.ZK_SERVICE_NAME)

  def apply(settings: ZkClientExtension#Settings): Behavior[Message] = {
    Behaviors.setup[Message] { context: ActorContext[Message] => 
      Behaviors.withStash(100) { stash: StashBuffer[Message] =>
        new ZkClient(context, settings, stash)
      }
    } 
  }

  object LifecycleStage extends Enumeration {
		type LifecycleStage = Value;

		val INITIAL = Value("Initial");
		val CONNECTED = Value("Connected");
		val SUSPENDED = Value("Suspended");
		val LOST = Value("Lost");
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
  class ZkClientInvalidStateException(val message: String) extends Exception(message) { }
  
  class ZkLostSessionException extends RuntimeException { }
}


class ZkClient(
	/*override*/ val context: ActorContext[ZkClient.Message],
	val settings: ZkClientExtension#Settings,
	val stash: StashBuffer[ZkClient.Message]
) extends AbstractBehavior[ZkClient.Message](context) with ZookeeperSessionWatcher {
//		val nodeSequenceModelClient: ModeledFramework[NodeSequenceModel],
//		val variantConfigModelClient: ModeledFramework[VariantConfigModel],
//		val versionedModelClient: VersionedModeledFramework[NodeSequenceModel],
//		val genClaimsSeq: PSequence[GeneratorIdClaimState] = TreePVector.empty(),
//		val genClaimsByActorRef: PMap[ActorRef[ZkEvents.Event], GeneratorIdClaimState] = HashTreePMap.empty(),
//		val genClaimsById: PMap[Long, GeneratorIdClaimState] = HashTreePMap.empty(),
//		val leaderLatch: Optional[LeaderLatch] = Optional.empty(),
//		val idGenConfigWatch: Optional[CuratorWatcher] = Optional.empty(),
//		val idGenConfigModel: Optional[Versioned[NodeSequenceModel]] = Optional.empty(),
//		val pendingIdClaimProposal: Optional[AsyncResult[NodeSequenceModel]] = Optional.empty(),
//		val pendingProposalNewIds: PSequence[Long] = TreePVector.empty()
	

//	private[zk] val claimSubscriberBehavior:
//	  PartialFunction[ZkClient.Request, Behavior[ZkClient.Request]] = { 
//	  case ZkClient.SubscribeGeneratorClientRequest(
//			replyTo: ActorRef[StatusReply[ZkClient.Response]],
//			subscriber: ActorRef[ZkEvents.Event]
//		) =>
//	    context.log.info("In claimSubsriberBehavior() subscription case")
//     	context.log.info("Still in claimSubsriberBehavior() subscription")
//     	replyTo ! StatusReply.success(ZkClient.GeneratorClientSubscribedResponse())
//     	context.log.info("Sent reply!")
//     	context.self ! ZkClientProtocol.RunClaimMaintenance()
//     	val newRequest = new RequestingGeneratorIdClaimState(subscriber)
//     	changeState(
//     		currentState.copy(
//     			genClaimsSeq = currentState.genClaimsSeq.plus(newRequest),
//     			genClaimsByActorRef = currentState.genClaimsByActorRef.plus(subscriber, newRequest)
//     		)
//     	)
//	  case ZkClient.UnsubscribeGeneratorClientRequest(
//			replyTo: ActorRef[StatusReply[ZkClient.Response]],
//			subscriber: ActorRef[ZkClientEvents.Event]
//		) =>
//	    val toRemove = currentState.genClaimsByActorRef.get(subscriber)
//	    toRemove match {
//	      case null =>
//	        replyTo ! StatusReply.error("No such subscriber to unsubscribe")
//	        Behaviors.unhandled
//	      case active: CurrentGeneratorIdClaimState =>
//	        replyTo ! StatusReply.error(
//			      new IllegalStateException("Release generator ID claim before canceling subscription")
//			    )
//	        Behaviors.unhandled
//	      case request: RequestingGeneratorIdClaimState =>
//	        subscriber ! new ZkClientEvents.DroppedRequestEvent(subscriber)
//	        replyTo ! StatusReply.success(ZkClient.GeneratorClientUnsubscribedResponse())
//	        changeState(
//			      currentState.copy(
//					    genClaimsSeq = currentState.genClaimsSeq.minus(toRemove),
//					    genClaimsByActorRef = currentState.genClaimsByActorRef.minus(subscriber)
//					  )
//			    )
//	      case request: ProposedGeneratorIdClaimState =>
//	        subscriber ! new ZkClientEvents.DroppedRequestEvent(subscriber)
//	        replyTo ! StatusReply.success(ZkClient.GeneratorClientUnsubscribedResponse())
//	        changeState(
//			      currentState.copy(
//					    genClaimsSeq = currentState.genClaimsSeq.minus(toRemove),
//					    genClaimsByActorRef = currentState.genClaimsByActorRef.minus(subscriber)
//					  )
//			    )
//	    }
//	}

//	def failed(): Behavior[ZkClient.Request] = { 
//	  context.log.info("In failed()")
//		Behaviors.receiveMessage(
//		  // claimSubscriberBehavior orElse {
//		  {
//				case src: ZkClient.AskedRequest =>
//				  src.replyTo ! StatusReply.error(s"Received {_.toString()} after failure")
//				  context.log.error(s"Received {_.toString()} after failure")
//				  Behaviors.same
//				case _ => 
//				  context.log.error(s"Received {_.toString()} after failure")
//				  Behaviors.same
//	    }
//		)
//	}

//	private[zk] val claimSubscriberBehavior:
//	  PartialFunction[ZkClient.Query, Behavior[ZkClient.Message]] = { 
//	    case ZkClient.GetClientRegistration( variantId: Byte,  queryId: Int,  replyTo: ActorRef[ZkClient.Response]
//		  ) => {
//		    if (! this.variantClients(variantId).isPresent()) {
//		      val thisVariantId = variantId
//		      val retVal = context.spawn(
//		        { (context: ActorContext[ZkClient.Request]) => 
//		          ZkClient(context, thisVariantId, settings)
//		        },
//		        s"ZkClient_$variantId"
//		      );
//		    }
//		  }
//     	replyTo ! StatusReply.success(ZkClient.GeneratorClientSubscribedResponse())
//     	context.log.info("Sent reply!")
//     	context.self ! ZkClientProtocol.RunClaimMaintenance()
//     	val newRequest = new RequestingGeneratorIdClaimState(subscriber)
//     	changeState(
//     		currentState.copy(
//     			genClaimsSeq = currentState.genClaimsSeq.plus(newRequest),
//     			genClaimsByActorRef = currentState.genClaimsByActorRef.plus(subscriber, newRequest)
//     		)
//     	)
//	val nodeSeqClient = NodeSequenceModel(curatorClient, settings, 1) 
//	val variantConfigClient = VariantConfigModel(curatorClient, settings, 1)
//	val versionedClient = nodeSeqClient.versioned()
  
  type BehaviorMode = ZkClient.Message => Behavior[ZkClient.Message];

  import ZkClient.LifecycleStage._
  
	this.context.log.info("Creating Zk Client from thread {Thread.currentThread()}")

//	val zPathToServiceKeys: HashMap[ZPath, ServiceKey[_]] =
//	  new HashMap[ZPath, ServiceKey[_]]()
	val curatorClient: AsyncCuratorFramework = AkkaCuratorClient(this.settings)
	val sessionWatchEventHandler: ActorRef[ZookeeperSessionWatcher.Event] =
	  context.messageAdapter( (src: ZookeeperSessionWatcher.Event) =>
	    src match {
	      case ZookeeperSessionWatcher.ZkConnectionSuccessful() => 
	        ZkClient.ZkConnectionSuccessful()
	      case ZookeeperSessionWatcher.ZkConnectionLost() => 
	        ZkClient.ZkConnectionLost()
	      case ZookeeperSessionWatcher.ZkConnectionSuspended() => 
	        ZkClient.ZkConnectionSuspended()
	      case ZookeeperSessionWatcher.ZkConnectionReconnected() => 
	        ZkClient.ZkConnectionReconnected()
	    }
	  )
	var currentBehavior: BehaviorMode = preSession _
	var currentLifecycleStage: LifecycleStage = INITIAL
		
//  val commonlyUnused: PartialFunction[ZkClient.Message, Behavior[ZkClient.Message]] = {
//    case ZkClient.ZkAcquiredLeadership() => 
//      this.context.log.error(
//        "Received impossible leadership notifcation although we are not participating in a leadership contest")
//      Behaviors.unhandled
//    case ZkClient.ZkLostLeadership() => 
//      this.context.log.error(
//        "Received impossible leadership notifcation although we are not participating in a leadership contest")
//      Behaviors.unhandled
//    case ZkClient.ZkProcessChildChange(event: WatchedEventMeta) => 
//      this.context.log.error(
//        "Received impossible changed children notification as we have not subscribed for any watches")
//      Behaviors.unhandled
//    case ZkClient.ZkProcessDataChange(event: WatchedEventMeta) => 
//      this.context.log.error(
//        "Received impossible changed children notification as we have not subscribed for any watches")
//      Behaviors.unhandled
//		case _ => 
//		  this.context.log.error(s"Unexpected message, {_.toString()}, received while client session is disconnected.")
//		  Behaviors.unhandled
//	}

//  val stashQueries: PartialFunction[ZkClient.Message, Behavior[ZkClient.Message]] = {
//    case msg @ ZkClient.GetZPathBasedActorRequest(
//      replyTo: ActorRef[StatusReply[ZkClient.Response]],
//      actorType: String,
//      pathToZNode: String
//    ) => 
//      this.context.log.warn("Received ZNode based actor lookup before bootstrap was complete.  Stashing to defer.")
//      stash.stash(msg)
//      this
//  }
//  
//  def maybeActorByTypeAndZPath(actorType: String, zPath: ZPath): Optional[ServiceKey[_]] = {
//    // TODO: We are momentarily disregarding the actorType part of this identifier, but that gap
//    //       will need to close as soon as we have more than one service or compoment type using
//    //       this functionality.
//    var retVal: Optional[ServiceKey[_]] = Optional.empty()
//    actorType match {
//      case Constants.ULID_ZPATH_ACTOR_TYPE => {
//        val result: ServiceKey[_] = zPathToServiceKeys(zPath)
//        if (result != null) {
//          retVal = Optional.of(result)
//        }
//      }
//      case _ => {
//        this.context.log.warn(
//          s"Only actorType == 'ulid' is supported, but was queried for $actorType")
//      }
//    }
//    retVal
//  }
//  
//  def ensureActorByTypeAndZPath(actorType: String, zPath: ZPath): ServiceKey[_] = {
//    // TODO: We are momentarily disregarding the actorType part of this identifier, but that gap
//    //       will need to close as soon as we have more than one service or compoment type using
//    //       this functionality.
//    actorType match {
//      case Constants.ULID_ZPATH_ACTOR_TYPE => {
//        val keyName = s"${actorType}@${zPath}"
//        val serviceKey = ServiceKey[ZkULIDAuthority](keyName) 
//        this.context.spawn(
//          ZkULIDAuthority(null), keyName
//        )
//        zPathToServiceKeys += (zPath -> serviceKey)
//        serviceKey
//      }
//      case _ => {
//        this.context.log.warn(
//          s"Only actorType == 'ulid' is supported, but was queried for $actorType")
//        null
//      }
//    }
//  }

  def suspended(msg: ZkClient.Message): Behavior[ZkClient.Message] = {
	  val partialFn: PartialFunction[ZkClient.Message, Behavior[ZkClient.Message]] = {
      case evt: ZkClient.ZkConnectionSuccessful => 
        this.context.log.error(
          "Received out-of-this.context session connection notice while suspended on an existing session"
        )
        Behaviors.unhandled
      case evt: ZkClient.ZkConnectionLost => 
        this.context.log.error("Received lost session notification.")
//        throw new ZkClient.ZkLostSessionException();
        ZkClient.this.currentBehavior = preSession _
        ZkClient.this
      case evt: ZkClient.ZkConnectionSuspended => 
        this.context.log.error(
          "Received out-of-this.context session suspension notice, but current session is already suspended")
        Behaviors.unhandled
      case evt: ZkClient.ZkConnectionReconnected => 
        this.context.log.info(
          "Received session reconnection notice.  Returning to connected state."
        )
        ZkClient.this.currentBehavior = connected _
        ZkClient.this
    }
	  
	  // Combine unique partial above with common partial subblocks, then
	  // execute.
	  // TODO: Attempt to construct this once and reuse it rather than
	  //       reassembling on each API interaction round trip.
	  partialFn.apply(msg)
//    .orElse(
//	    ZkClient.this.stashQueries
//	  ).orElse(partialFn
//	    ZkClient.this.commonlyUnused
//	  ).apply(msg)
  }
  
  def connected(msg: ZkClient.Message): Behavior[ZkClient.Message] = { 
	  val partialFn: PartialFunction[ZkClient.Message, Behavior[ZkClient.Message]] = {
      case evt: ZkClient.ZkConnectionSuccessful => 
        this.context.log.warn(
          "Received redundant 'Connected' event while already in an open ZK Session."
        )
        Behaviors.unhandled
      case evt: ZkClient.ZkConnectionLost => 
        this.context.log.error("Received lost session notification.")
        ZkClient.this.currentBehavior = preSession _
        ZkClient.this
      case evt: ZkClient.ZkConnectionSuspended => 
        this.context.log.info("Processing session suspension notice")
        ZkClient.this.currentBehavior = suspended _
        Behaviors.same
      case evt: ZkClient.ZkConnectionReconnected => 
        this.context.log.error(
          "Received out-of-this.context session resumption notice, but current session is not suspended.")
        Behaviors.unhandled
//      case msg @ ZkClient.GetZPathBasedActorRequest(
//        replyTo: ActorRef[StatusReply[ZkClient.Response]],
//        actorType: String,
//        zPath: String
//      ) => 
//        this.context.log.info(
//          s"Servicing query for ZPath-based actor type $actorType at $zPath") 
//        val parsedZPath = ZPath.parse(zPath)
//        val foundKey: Optional[ServiceKey[_]] =
//          this.maybeActorByTypeAndZPath(actorType, parsedZPath)
//        if (foundKey.isPresent()) {
//          replyTo ! StatusReply.success(
//            ZkClient.GetZPathBasedActorResponse(
//              actorType, zPath, foundKey.get()
//            )
//          ) 
//        } else {
//          val createdKey: ServiceKey[_] = this.ensureActorByTypeAndZPath(
//            actorType, parsedZPath)
//          if (createdKey == null) {
//            replyTo ! StatusReply.error("Could not allocate key for")
//          } else {
//            this.zPathToServiceKeys += (parsedZPath -> createdKey)
//            replyTo ! StatusReply.success(
//              ZkClient.GetZPathBasedActorResponse(
//                actorType, zPath, createdKey
//              )
//            )
//          }
//        }
        this
	  }

	  partialFn.apply(msg)
  }
		
  def preSession(msg: ZkClient.Message): Behavior[ZkClient.Message] = {
	  val partialFn: PartialFunction[ZkClient.Message, Behavior[ZkClient.Message]] = {
      case evt: ZkClient.ZkConnectionSuccessful => 
        this.context.log.info(
          "Incoming login ack steps field status to connected"
        )
        currentBehavior = connected
        stash.unstashAll(this)
      case evt: ZkClient.ZkConnectionLost => 
        this.context.log.error(
          "Received out-of-this.context session loss notice before having connected to a session"
        )
        Behaviors.unhandled
      case evt: ZkClient.ZkConnectionSuspended => 
        this.context.log.error(
          "Received out-of-this.context session suspension notice, but no connection is open yet")
        Behaviors.unhandled
      case evt: ZkClient.ZkConnectionReconnected => 
        this.context.log.error(
          "Received out-of-this.context session resumption notice, but no connection is open yet")
        Behaviors.unhandled
	  }

	  partialFn.apply(msg)
	}
			//      this.context.log.info(s"Zk bootstrap pending on {Thread.currentThread()}")
			//      handle(state)
			//              handle(new BootstrappingState(settings, client)) // closeableServices))
			//        case ZkClient.claimGenIdReq: ClaimGeneratorIdRequest =>
			//              Behaviors.same
			//                val myId = System.getenv("HOSTNAME")
			//                val group = new GroupMember(client, settings.ZK_ZNODE, myId, new Array[Byte](3))
			//                var seedEntryAdded = false
			//                val closeableServices: PSet[Closeable] = HashTreePSet.singleton(group)

//	def changeState(nextState: State, nextBehavior: Behavior[ZkClient.Request] = currentBehavior): Behavior[ZkClient.Request] = {
//		currentState = nextState
//		currentBehavior = nextBehavior
//		currentBehavior
//	}
//
//	def changeBehavior(nextBehavior: Behavior[ZkClient.Request]): Behavior[ZkClient.Request] = {
//    currentBehavior = nextBehavior
//	  currentBehavior
//	}

//	def publishEvent(nextEvent: ZkEvents.Event): Unit =
//		nextEvent match {
//			case genIdEvent: ZkEvents.GeneratorIdEvent =>
//				genIdEvent.subscriber ! genIdEvent
//	  	case clientEvent: ZkEvents.ZookeeperSessionEvent =>
//				currentState.genClaimsSeq.forEach { claimState => 
//				  claimState.subscriber ! clientEvent
//		    }
//	  }
  
  /*
  val suspended: Function1[ZkClient.Message, Behavior[ZkClient.Message]] = { msg: ZkClient.Message =>
	  msg match {
				case ZkClient.ZkConnectionSuccessful() =>
					publishEvent(ZkClientEvents.SessionConnectedEvent())
					changeBehavior(connected)
				case ZkClient.ZkConnectionLost()  =>
					publishEvent(ZkClientEvents.SessionLostEvent())
					changeBehavior(disconnected)
				case ZkClient.ZkConnectionPastDeadline() =>
					publishEvent(ZkClientEvents.FailedToConnectEvent())
					changeBehavior(failed)
				case ZkClient.ZkProcessChildChange(event: WatchedEventMeta)  =>
					Behaviors.same
				case ZkClient.ZkProcessDataChange(event: WatchedEventMeta)  =>
					Behaviors.same
			}
   */

//	def publishEvent(nextEvent: ZkClient.ZookeeperSessionEvent): Unit =
//		currentState.genClaimsSeq.forEach { claimState => 
//			claimState.subscriber ! nextEvent
//	  }

	override def onMessage(msg: ZkClient.Message): Behavior[ZkClient.Message] = {
		this.context.log.info(s"Called onMessage($msg)")
		currentBehavior(msg)
	}
}


