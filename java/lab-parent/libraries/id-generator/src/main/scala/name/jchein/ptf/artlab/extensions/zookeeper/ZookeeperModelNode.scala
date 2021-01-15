package name.jchein.ptf.artlab.extensions.zookeeper



import scala.language.higherKinds

import org.apache.curator.framework.CuratorFramework
import org.apache.curator.x.async.AsyncCuratorFramework
import org.apache.curator.x.async.modeled.ModelSpec
import org.apache.curator.x.async.modeled.ModeledFramework
import org.apache.curator.x.async.modeled.ZNode
import org.apache.curator.x.async.modeled.ZPath
import org.apache.zookeeper.data.Stat

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.StashBuffer
import akka.pattern.StatusReply


object ZookeeperModelNode {
//  type Mdl
  sealed trait Message[Mdl]
  
  sealed trait Event[Mdl]
  final case class ZkDataCacheUpdated[Mdl](val znode: ZNode[Mdl]) extends Event[Mdl]
  final case class ZkDataChangeObserved[Mdl](val event: WatchedEventMeta) extends Event[Mdl]

  sealed trait Response[Mdl]
  final case class SubscribeToCacheResponse[Mdl]() extends ZookeeperModelNode.Response[Mdl]
  final case class ReadDataVersionResponse[Mdl](val currentData: Mdl, currentVersion: Int) extends Response[Mdl]
  final case class WriteDataVersionResponse[Mdl](val nextVersion: Int) extends Response[Mdl]
  final case class CreatePersistentChildResponse[Mdl](val stat: Stat) extends Response[Mdl]
  final case class VerifyChildExistsResponse[Mdl](val stat: Stat) extends Response[Mdl]

  sealed trait Request[Mdl, Rsp <: ZookeeperModelNode.Response[Mdl]] extends Message[Mdl] {
    val replyTo: ActorRef[StatusReply[Rsp]]
  }

  final case class SubscribeToCacheRequest[Mdl, SubscribeToCacheResponse[Mdl]](
    override val replyTo: ActorRef[StatusReply[ZookeeperModelNode.SubscribeToCacheResponse[Mdl]]],
    val subscriber: ActorRef[Event[Mdl]]
  ) extends Request[Mdl, ZookeeperModelNode.SubscribeToCacheResponse[Mdl]]

  final case class ReadDataVersionRequest[Mdl, ReadDataVersionResponse[Mdl]](
    val replyTo: ActorRef[StatusReply[ZookeeperModelNode.ReadDataVersionResponse[Mdl]]]
  ) extends Request[Mdl, ZookeeperModelNode.ReadDataVersionResponse[Mdl]]

  final case class WriteDataVersionRequest[Mdl, WriteDataVersionResponse[Mdl]](
    val replyTo: ActorRef[StatusReply[ZookeeperModelNode.WriteDataVersionResponse[Mdl]]],
    val nextState: Mdl,
    val currentVersion: Int
  ) extends Request[Mdl, ZookeeperModelNode.WriteDataVersionResponse[Mdl]]

  final case class CreatePersistentChildRequest[Mdl, CreatePersistentChildResponse[Mdl]](
    val replyTo: ActorRef[StatusReply[ZookeeperModelNode.CreatePersistentChildResponse[Mdl]]],
    val relativePath: ZPath,
    val data: Mdl,
    val ttl: Long
  ) extends Request[Mdl, ZookeeperModelNode.CreatePersistentChildResponse[Mdl]]

  final case class VerifyChildExistsRequest[Mdl, VerifyChildExistsResponse[Mdl]](
    val replyTo: ActorRef[StatusReply[ZookeeperModelNode.VerifyChildExistsResponse[Mdl]]],
    val relativePath: ZPath
  ) extends Request[Mdl, ZookeeperModelNode.VerifyChildExistsResponse[Mdl]]

  private[zookeeper] sealed trait InternalEvent[Mdl] extends Message[Mdl]
  private[zookeeper] final case class WrappedZkDataEvent[Mdl](
    event: ZookeeperDataWatcher.Event
  ) extends InternalEvent[Mdl]
  private[zookeeper] final case class ZkConnectionSuccessful[Mdl]() extends InternalEvent[Mdl]
  private[zookeeper] final case class ZkConnectionLost[Mdl]() extends InternalEvent[Mdl]
  private[zookeeper] final case class ZkConnectionSuspended[Mdl]() extends InternalEvent[Mdl]
  private[zookeeper] final case class ZkConnectionReconnected[Mdl]() extends InternalEvent[Mdl]
  
  def apply[Mdl](
    curator: CuratorFramework, modelSpec: ModelSpec[Mdl]
  ): Behavior[ZookeeperModelNode.Message[Mdl]] = {
    Behaviors.setup[ZookeeperModelNode.Message[Mdl]](
      (context: ActorContext[ZookeeperModelNode.Message[Mdl]]) => 
        Behaviors.withStash(100){
          (stash: StashBuffer[ZookeeperModelNode.Message[Mdl]]) =>
            new ZookeeperModelNode[Mdl](context, curator, modelSpec, stash) 
        }
    )
  }
}

class ZookeeperModelNode[T](
	override val context: ActorContext[ZookeeperModelNode.Message[T]], 
	val curator: CuratorFramework, 
	val modelSpec: ModelSpec[T], 
	val stash: StashBuffer[ZookeeperModelNode.Message[T]]
) extends AbstractBehavior[ZookeeperModelNode.Message[T]](context)
  with ZookeeperDataWatcher
  with ZookeeperSessionWatcher {

  type BehaviorMode = ZookeeperModelNode.Message[T] =>
    Behavior[ZookeeperModelNode.Message[T]];

	this.context.log.info("Creating Zookeeper Model Client from thread {Thread.currentThread()}")

	val asyncCurator: AsyncCuratorFramework = AsyncCuratorFramework.wrap(curator);
	val modelClient: ModeledFramework[T] = ModeledFramework.wrap(asyncCurator, modelSpec)

	val sessionWatchEventHandler: ActorRef[ZookeeperSessionWatcher.Event] =
	  context.messageAdapter[ZookeeperSessionWatcher.Event](
	    (src: ZookeeperSessionWatcher.Event) => {
  	    src match {
  	      case evt: ZookeeperSessionWatcher.ZkConnectionSuccessful => 
  	        ZookeeperModelNode.ZkConnectionSuccessful()
  	      case evt: ZookeeperSessionWatcher.ZkConnectionLost => 
  	        ZookeeperModelNode.ZkConnectionLost()
  	      case evt: ZookeeperSessionWatcher.ZkConnectionSuspended => 
  	        ZookeeperModelNode.ZkConnectionSuspended()
  	      case evt: ZookeeperSessionWatcher.ZkConnectionReconnected => 
  	        ZookeeperModelNode.ZkConnectionReconnected()
  	    }
  	  }
	  )
	val dataWatchEventHandler: ActorRef[ZookeeperDataWatcher.Event] = 
	  context.messageAdapter[ZookeeperDataWatcher.Event](ZookeeperModelNode.WrappedZkDataEvent[T])

  val commonlyUnused: PartialFunction[ZookeeperModelNode.Message[T], Behavior[ZookeeperModelNode.Message[T]]] = {
//    case ZookeeperClient.ZkProcessChildChange(event: WatchedEventMeta) => 
//      this.context.log.error(
//        "Received impossible changed children notification as we have not subscribed for any watches")
//      Behaviors.unhandled
		case _ => 
		  this.context.log.error(s"Unexpected message, {_.toString()}, received while client session is disconnected.")
		  Behaviors.unhandled
	}
  def onMessage(msg: ZookeeperModelNode.Message[T]): Behavior[ZookeeperModelNode.Message[T]] = {
	  ???
	}

//	def publishEvent(nextEvent: ZookeeperModelNode.ZkSessionEvent): Unit =
//		currentState.genClaimsSeq.forEach { claimState => 
//			claimState.subscriber ! nextEvent
//	  }
}


