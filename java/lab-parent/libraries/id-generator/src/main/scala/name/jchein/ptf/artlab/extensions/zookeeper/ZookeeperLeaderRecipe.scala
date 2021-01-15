package name.jchein.ptf.artlab.extensions.zookeeper

import java.nio.ByteBuffer
import java.util.Optional

import scala.collection.mutable.HashMap

import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.leader.LeaderLatch
import org.apache.curator.framework.recipes.leader.LeaderLatch.CloseMode
import org.apache.curator.x.async.AsyncCuratorFramework
import org.apache.curator.x.async.modeled.ModelSpec
import org.apache.curator.x.async.modeled.ZNode
import org.apache.curator.x.async.modeled.ZPath
import org.apache.curator.x.async.modeled.versioned.Versioned
import org.apache.curator.x.async.modeled.ModeledFramework
import org.apache.zookeeper.data.Stat

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.StashBuffer
import akka.pattern.StatusReply

import name.jchein.ptf.artlab.extensions.id_generator.zookeeper.ZookeeperULIDAuthority
import scala.collection.mutable.HashMap

object ZookeeperLeaderRecipe {
  type Mdl
  sealed trait Message

  sealed trait Request[Resp <: ZookeeperLeaderRecipe.Response] extends Message {
    val replyTo: ActorRef[StatusReply[Resp]]
  }
  final case class AcquireLeaderLatchRequest(
    val replyTo: ActorRef[StatusReply[AcquireLeaderLatchResponse]],
    val subscriber: ActorRef[Event]
  ) extends Request[AcquireLeaderLatchResponse]
  final case class ReleaseLeaderLatchRequest(
    val replyTo: ActorRef[StatusReply[ReleaseLeaderLatchResponse]],
    val releaseHandle: Object
  ) extends Request[ReleaseLeaderLatchResponse]

  sealed trait Response
  final case class AcquireLeaderLatchResponse(val releaseHandle: Object) extends Response
  final case class ReleaseLeaderLatchResponse(val releaseHandle: Object) extends Response

  sealed trait Event
  final case class LeaderLatchStarting(val releaseHandle: Object) extends Event
  final case class LeaderRoleAcquired(val releaseHandle: Object) extends Event
  final case class LeaderRoleLost(val releaseHandle: Object) extends Event
  final case class LeaderLatchClosed(val releaseHandle: Object) extends Event
  final case class LeaderLatchFailure(val releaseHandle: Object, val err: Throwable) extends Event
  
  private[zookeeper] sealed trait InternalEvent extends Message
  private[zookeeper] final case class StartLeaderLatchRequest(
    val latchHolder: ZookeeperLeaderRecipe.LeaderLatchHolder
  ) extends InternalEvent
  private[zookeeper] final case class CloseLeaderLatchRequest(
    val latchHolder: ZookeeperLeaderRecipe.LeaderLatchHolder
  ) extends InternalEvent
  private[zookeeper] final case class WrappedZkLeaderEvent(
    val releaseHandle: Object,
    val event: ZookeeperLeaderWatcher.Event
  ) extends InternalEvent
  private[zookeeper] final case class ZkConnectionSuccessful() extends InternalEvent
  private[zookeeper] final case class ZkConnectionLost() extends InternalEvent
  private[zookeeper] final case class ZkConnectionSuspended() extends InternalEvent
  private[zookeeper] final case class ZkConnectionReconnected() extends InternalEvent

//12 1:45 1 888 334 1000 my health online options 
  def apply(
    curator: CuratorFramework,
    zpath: ZPath,
    stashBufferSize: Int = 0 // , nameId: String
  ): Behavior[ZookeeperLeaderRecipe.Message] = {
    Behaviors.setup[ZookeeperLeaderRecipe.Message](
      (context: ActorContext[ZookeeperLeaderRecipe.Message]) => {
        val nameId: String = s"${context.system.name}@${context.system.address}"
        if (stashBufferSize > 0) {
          Behaviors.withStash(stashBufferSize)(
            (stash: StashBuffer[ZookeeperLeaderRecipe.Message]) =>
              new ZookeeperLeaderRecipe(context, curator, stash, zpath, nameId) 
          )
        } else {
          new ZookeeperLeaderRecipe(context, curator, null, zpath, nameId)
        }
      }
    )
  }

  class LeaderLatchHolder(
    val context: ActorContext[ZookeeperLeaderRecipe.Message],
    val subscriber: ActorRef[ZookeeperLeaderRecipe.Event],
    val leaderLatch: LeaderLatch,
    val releaseHandle: Object,
  ) extends ZookeeperLeaderWatcher {
	  val leaderWatchEventHandler: ActorRef[ZookeeperLeaderWatcher.Event] = 
	    context.messageAdapter(
	      evt => ZookeeperLeaderRecipe.WrappedZkLeaderEvent(releaseHandle, evt)
	    )
	    
	  leaderLatch.addListener(this)
	    
	  def start(): Unit = {
	    leaderLatch.start();
	  }
	  
	  override def equals(other: Any): Boolean = {
	    this.releaseHandle.equals(other)
	  }
	  
	  override def hashCode(): Int = {
	    this.releaseHandle.hashCode
	  }
  }
}

class ZookeeperLeaderRecipe(
	override val context: ActorContext[ZookeeperLeaderRecipe.Message],  
	val curator: CuratorFramework,  
	val stash: StashBuffer[ZookeeperLeaderRecipe.Message],
	val zpath: ZPath, 
	val nameId: String
) extends AbstractBehavior[ZookeeperLeaderRecipe.Message](context)
  with ZookeeperSessionWatcher {
  type Message = ZookeeperLeaderRecipe.Message
  type Event = ZookeeperLeaderRecipe.Event
  type ZkConnectionLost = ZookeeperSessionWatcher.ZkConnectionLost
  type ZkConnectionReconnected = ZookeeperSessionWatcher.ZkConnectionReconnected
  type ZkConnectionSuccessful = ZookeeperSessionWatcher.ZkConnectionSuccessful
  type ZkConnectionSuspended = ZookeeperSessionWatcher.ZkConnectionSuspended

  type LifecycleStage = ZookeeperClient.LifecycleStage.LifecycleStage
  val INITIAL = ZookeeperClient.LifecycleStage.INITIAL
  val CONNECTED = ZookeeperClient.LifecycleStage.CONNECTED
  val SUSPENDED = ZookeeperClient.LifecycleStage.SUSPENDED
  val LOST = ZookeeperClient.LifecycleStage.LOST
  
  type PartialBehaviorFunction = PartialFunction[
    ZookeeperLeaderRecipe.Message, 
    Behavior[ZookeeperLeaderRecipe.Message]
  ]
  type BehaviorFunction = (ZookeeperLeaderRecipe.Message) =>
    Behavior[ZookeeperLeaderRecipe.Message]

	this.context.log.info("Creating Zookeeper Model Client from thread {Thread.currentThread()}")

	val latchHandles: HashMap[Object, ZookeeperLeaderRecipe.LeaderLatchHolder] =
	  HashMap[Object, ZookeeperLeaderRecipe.LeaderLatchHolder]();

	val sessionWatchEventHandler: ActorRef[ZookeeperSessionWatcher.Event] =
	  context.messageAdapter((src: ZookeeperSessionWatcher.Event) =>
	    src match {
	      case evt: ZkConnectionSuccessful => 
	        new ZookeeperLeaderRecipe.ZkConnectionSuccessful()
	      case evt: ZkConnectionLost => 
	        new ZookeeperLeaderRecipe.ZkConnectionLost()
	      case evt: ZkConnectionSuspended => 
	        new ZookeeperLeaderRecipe.ZkConnectionSuspended()
	      case evt: ZkConnectionReconnected => 
	        new ZookeeperLeaderRecipe.ZkConnectionReconnected()
	    }
	  )
	  
	var currentBehavior: BehaviorFunction = deferRemoteCallsProtocol
	  .orElse(acceptRequestsProtocol)
	  .orElse(rejectLeaderEventsProtocol)
	  .orElse(offlineSessionProtocol)

	def routeToStash(msg: ZookeeperLeaderRecipe.Message): Behavior[ZookeeperLeaderRecipe.Message] = {
	  if (stash.isFull) {
	    this.context.log.error(
	      s"Dropping <$msg> due to stash overflow while session not yet connected."
	    )
      Behaviors.unhandled
    } else {
      this.stash.stash(msg)
      Behaviors.same 
    }
	}

	val deferRemoteCallsProtocol: PartialBehaviorFunction = {
    case req: ZookeeperLeaderRecipe.StartLeaderLatchRequest => {
      routeToStash(req) 
    }
	  case req: ZookeeperLeaderRecipe.CloseLeaderLatchRequest => {
      routeToStash(req)
	  }
	}
	
	val acceptRemoteCallsProtocol: PartialBehaviorFunction = {
    case req: ZookeeperLeaderRecipe.StartLeaderLatchRequest => {
      val adapter: ZookeeperLeaderRecipe.LeaderLatchHolder = req.latchHolder
      try {
        adapter.start()
      } catch {
        case err: Throwable => {
          context.log.error("Failed to start leader latch: ", err)
          req.latchHolder.subscriber !
            ZookeeperLeaderRecipe.LeaderLatchFailure(adapter.releaseHandle, err)
          this.latchHandles.remove(adapter.releaseHandle)
        }
      }
      this
    }
	  case req: ZookeeperLeaderRecipe.CloseLeaderLatchRequest => {
      val adapter: ZookeeperLeaderRecipe.LeaderLatchHolder = req.latchHolder
      try {
        adapter.leaderLatch.close()
        adapter.subscriber !
          ZookeeperLeaderRecipe.LeaderLatchClosed(adapter.releaseHandle)
      } catch {
        case err: Throwable => {
          adapter.subscriber !
            ZookeeperLeaderRecipe.LeaderLatchFailure(adapter.releaseHandle, err)
        }
      } finally {
        latchHandles.remove(adapter.releaseHandle)
      }
      this
	  }
	}

	val acceptRequestsProtocol: PartialBehaviorFunction = {
    case req: ZookeeperLeaderRecipe.AcquireLeaderLatchRequest => {
      val handle: Object = new Object()
      val latch: LeaderLatch = new LeaderLatch(
        curator, zpath.fullPath(), nameId, CloseMode.NOTIFY_LEADER
      )
      val adapter: ZookeeperLeaderRecipe.LeaderLatchHolder =
        new ZookeeperLeaderRecipe.LeaderLatchHolder(
          context, req.subscriber, latch, handle)
      this.latchHandles += (handle -> adapter)
      context.self ! 
        ZookeeperLeaderRecipe.StartLeaderLatchRequest(adapter)
      req.replyTo ! StatusReply.success(
        ZookeeperLeaderRecipe.AcquireLeaderLatchResponse(handle)
      )
      this
    }
	  case req: ZookeeperLeaderRecipe.ReleaseLeaderLatchRequest => {
      val handle: Object = req.releaseHandle
      val adapterOption: Option[ZookeeperLeaderRecipe.LeaderLatchHolder] =
        latchHandles.get(handle)
      if (adapterOption.isEmpty) {
        context.log.error("Failed to locate latch for release request")
        req.replyTo ! StatusReply.error(
          new IllegalArgumentException("No latch found for provided latch release handle")
        )
      } else {
        context.self ! 
          ZookeeperLeaderRecipe.CloseLeaderLatchRequest(
            adapterOption.get)
        req.replyTo ! StatusReply.success(
          ZookeeperLeaderRecipe.ReleaseLeaderLatchResponse(handle)
        )
      }
      this
	  }
	}
	
	val rejectLeaderEventsProtocol: PartialBehaviorFunction = {
	  case evt: ZookeeperLeaderRecipe.WrappedZkLeaderEvent => {
      context.log.error(
        "Should not receive leader event triggers before ZK Client is Active")
      throw new IllegalStateException(
        "Should not receive leader event triggers before ZK Client is Active")
	  }
	}
	
	val acceptLeaderEventsProtocol: PartialBehaviorFunction = {
	  case ZookeeperLeaderRecipe.WrappedZkLeaderEvent(
	    releaseHandle: Object, evt: ZookeeperLeaderWatcher.ZkAcquiredLeaderRole
	  ) => {
      val adapterOption: Option[ZookeeperLeaderRecipe.LeaderLatchHolder] =
        latchHandles.get(releaseHandle)
      if (adapterOption.isEmpty) {
        context.log.error("Failed to locate latch for leadership acquisition event")
        throw new IllegalStateException("No latch holder found for elected latch handle")
      }
      val adapter: ZookeeperLeaderRecipe.LeaderLatchHolder =
        adapterOption.get
      adapter.subscriber !
        ZookeeperLeaderRecipe.LeaderRoleAcquired(releaseHandle)
      this
	  }
	  case ZookeeperLeaderRecipe.WrappedZkLeaderEvent(
	    releaseHandle: Object, evt: ZookeeperLeaderWatcher.ZkLostLeaderRole
	  ) => {
      val adapterOption: Option[ZookeeperLeaderRecipe.LeaderLatchHolder] =
        latchHandles.get(releaseHandle)
      if (adapterOption.isEmpty) {
        context.log.error("Failed to locate latch for leadership acquisition event")
        throw new IllegalStateException("No latch holder found for demoted latch handle")
      }
      val adapter: ZookeeperLeaderRecipe.LeaderLatchHolder =
        adapterOption.get
      adapter.subscriber !
        ZookeeperLeaderRecipe.LeaderRoleLost(releaseHandle)
      this
	  }
	}
	
  val offlineSessionProtocol: PartialBehaviorFunction = {
    case evt: ZookeeperLeaderRecipe.ZkConnectionSuccessful => {
      this.context.log.info(
        "Incoming login ack steps field status to connected"
      )
      changeBehavior(INITIAL, CONNECTED)
    }
    case evt: ZookeeperLeaderRecipe.ZkConnectionLost => {
      this.context.log.error(
        "Received out of context session loss notice before having connected to a session"
      )
      Behaviors.unhandled
    }
    case evt: ZookeeperLeaderRecipe.ZkConnectionSuspended => {
      this.context.log.error(
        "Received out of context session suspension notice, but no connection is open yet")
      Behaviors.unhandled
    }
    case evt: ZookeeperLeaderRecipe.ZkConnectionReconnected => {
      this.context.log.error(
        "Received out of context session resumption notice, but no connection is open yet")
      Behaviors.unhandled
	  }
  }
	
	val onlineSessionProtocol: PartialBehaviorFunction = {
    case ZookeeperLeaderRecipe.ZkConnectionSuccessful() => 
      this.context.log.warn(
        "Received redundant 'Connected' event while already in an open ZK Session."
      )
      Behaviors.unhandled
    case ZookeeperLeaderRecipe.ZkConnectionLost() => 
      this.context.log.error(
        "Received session loss notification.  Erroring out this actor"
      );
      changeBehavior(CONNECTED, LOST)
    case ZookeeperLeaderRecipe.ZkConnectionSuspended() => 
      this.context.log.warn("Processing session suspension notice")
      changeBehavior(CONNECTED, SUSPENDED)
    case ZookeeperLeaderRecipe.ZkConnectionReconnected() => 
      this.context.log.error(
        "Received out of context session resumption notice, but current session is not suspended.")
      Behaviors.unhandled
  }

  val suspendedSessionProtocol: PartialBehaviorFunction = {
    case ZookeeperLeaderRecipe.ZkConnectionSuccessful() => {
      this.context.log.error(
        "Received out of context session connection notice while suspended on an existing session"
      )
      Behaviors.unhandled
    }
    case ZookeeperLeaderRecipe.ZkConnectionLost() => {
      this.context.log.error(
        "Received session loss notification.  Erroring out this actor"
      );
      changeBehavior(SUSPENDED, LOST)
    }
    case ZookeeperLeaderRecipe.ZkConnectionSuspended() => {
      this.context.log.warn(
        "Received redundant session suspension notice, but current session is already suspended")
      Behaviors.unhandled
    }
    case ZookeeperLeaderRecipe.ZkConnectionReconnected() => {
      this.context.log.info(
        "Received session reconnection notice.  Returning to connected state."
      )
      changeBehavior(SUSPENDED, CONNECTED)
    }
  }
      
//	def publishEvent(nextEvent: ZookeeperLeaderRecipe.Event): Unit =
//		currentState.genClaimsSeq.forEach { claimState => 
//			claimState.subscriber ! nextEvent
//	  }

	protected val changeSessionBehavior: PartialFunction[(LifecycleStage, LifecycleStage), PartialBehaviorFunction] = {
	  case (_, INITIAL) => this.offlineSessionProtocol
	  case (_, CONNECTED) => this.onlineSessionProtocol
	  case (_, SUSPENDED) => this.suspendedSessionProtocol
	  case (_, LOST) => this.offlineSessionProtocol
	}
	
	protected val changeAppBehavior: PartialFunction[(LifecycleStage, LifecycleStage), PartialBehaviorFunction] = {
	  case (_, INITIAL) => this.acceptRequestsProtocol
	    .orElse(this.deferRemoteCallsProtocol)
	    .orElse(this.rejectLeaderEventsProtocol)
	  case (_, CONNECTED) => this.acceptRequestsProtocol
	    .orElse(this.acceptRemoteCallsProtocol)
	    .orElse(this.acceptLeaderEventsProtocol)
	  case (_, SUSPENDED) => this.acceptRequestsProtocol
	    .orElse(this.deferRemoteCallsProtocol)
	    .orElse(this.acceptLeaderEventsProtocol)
	  case (_, LOST) => this.acceptRequestsProtocol
	    .orElse(this.deferRemoteCallsProtocol)
	    .orElse(this.acceptLeaderEventsProtocol)
	}
	
  def changeBehavior(from: LifecycleStage, to: LifecycleStage): Behavior[ZookeeperLeaderRecipe.Message] = {
    if (to == INITIAL) {
      context.log.error("No transitions lead back to INITIAL stage")
      Behaviors.unhandled
    } else {
      val sessionBehavior: PartialBehaviorFunction = 
        changeSessionBehavior.apply(from, to)
      val appBehavior: PartialBehaviorFunction = 
        changeAppBehavior.apply(from, to)
      this.currentBehavior = 
        sessionBehavior.orElse(appBehavior)
      this
    }
	}

  def onMessage(msg: ZookeeperLeaderRecipe.Message): Behavior[ZookeeperLeaderRecipe.Message] = {
	  this.currentBehavior(msg)
	}

}
