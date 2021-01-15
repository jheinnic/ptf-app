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

object ZookeeperLeaderWatcher {
  sealed trait Event 
  final case class ZkAcquiredLeaderRole() extends Event
  final case class ZkLostLeaderRole() extends Event
}

trait LeaderWatchingActor {
    val leaderWatchEventHandler: ActorRef[ZookeeperLeaderWatcher.Event]
}

/**
 * Curator leader latch watching mix-in.
 *
 * Contains the logic for handling Curator [[org.apache.curator.framework.recipes.leader.LeaderLatchListener]]
 */
trait ZookeeperLeaderWatcher
extends LeaderLatchListener 
with LeaderWatchingActor {
  override def isLeader: Unit = {
    this.leaderWatchEventHandler ! ZookeeperLeaderWatcher.ZkAcquiredLeaderRole()
  }

  override def notLeader(): Unit = {
    this.leaderWatchEventHandler ! ZookeeperLeaderWatcher.ZkLostLeaderRole()
  }
}