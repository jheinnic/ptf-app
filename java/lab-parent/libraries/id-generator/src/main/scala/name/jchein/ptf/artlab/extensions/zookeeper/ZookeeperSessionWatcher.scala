package name.jchein.ptf.artlab.extensions.zookeeper

import org.apache.curator.framework.recipes.leader.LeaderLatchListener
import org.apache.zookeeper.WatchedEvent
import org.apache.zookeeper.Watcher.Event.KeeperState
import org.apache.zookeeper.Watcher
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.ActorRef
import org.apache.curator.framework.state.ConnectionStateListener
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.state.ConnectionState
import org.apache.curator.x.async.AsyncCuratorFramework
import akka.actor.typed.ActorRef.ActorRefOps
import akka.actor.typed.scaladsl.AbstractBehavior

object ZookeeperSessionWatcher {
  sealed trait Event
  final case class ZkConnectionSuccessful() extends Event
  final case class ZkConnectionLost() extends Event
  final case class ZkConnectionSuspended() extends Event
  final case class ZkConnectionReconnected() extends Event
}

trait SessionWatchingActor {
    val sessionWatchEventHandler: ActorRef[ZookeeperSessionWatcher.Event]
}

/**
 * Zookeeper client session status watching mix-in
 */
trait ZookeeperSessionWatcher
extends ConnectionStateListener 
with SessionWatchingActor {
  override def stateChanged(client: CuratorFramework, newState: ConnectionState): Unit = 
		newState match {
			case ConnectionState.CONNECTED =>
			  this.sessionWatchEventHandler ! ZookeeperSessionWatcher.ZkConnectionSuccessful()
			case ConnectionState.LOST =>
			  this.sessionWatchEventHandler ! ZookeeperSessionWatcher.ZkConnectionLost()
			case ConnectionState.READ_ONLY =>
			  None
			case ConnectionState.RECONNECTED =>
			  this.sessionWatchEventHandler ! ZookeeperSessionWatcher.ZkConnectionReconnected()
			case ConnectionState.SUSPENDED =>
			  this.sessionWatchEventHandler ! ZookeeperSessionWatcher.ZkConnectionSuspended()
  }
}