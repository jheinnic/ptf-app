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

object ZookeeperDataWatcher {
  sealed trait Event
  final case class ZkProcessChildChange(event: WatchedEventMeta) extends Event
  final case class ZkProcessDataChange(val event: WatchedEventMeta) extends Event
}

trait DataWatchingActor {
    val dataWatchEventHandler: ActorRef[ZookeeperDataWatcher.Event]
}

/**
 * ZooKeeper client data/children watching mix-in.
 *
 * Contains the logic for handling ZooKeeper [[org.apache.zookeeper.WatchedEvent]]
 */
trait ZookeeperDataWatcher extends Watcher with DataWatchingActor {
  private var keeperState = KeeperState.Disconnected

  /**
   * Process an incoming [[org.apache.zookeeper.WatchedEvent]].
   * @param event event to process
   */
  override def process(event: WatchedEvent): Unit = {
    val meta = WatchedEventMeta(event)

    if (meta.dataChanged) {
      this.dataWatchEventHandler ! ZookeeperDataWatcher.ZkProcessDataChange(meta)
    }

    if (meta.childrenChanged) {
      this.dataWatchEventHandler ! ZookeeperDataWatcher.ZkProcessChildChange(meta)
    }
  }
}