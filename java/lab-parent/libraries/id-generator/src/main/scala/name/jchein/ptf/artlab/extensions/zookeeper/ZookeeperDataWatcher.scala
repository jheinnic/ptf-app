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
import org.apache.zookeeper.Watcher.Event.EventType

object ZookeeperDataWatcher {
  sealed trait Event
  final case class WatchedChildrenChangedEvent(event: WatchedEvent) extends Event
  final case class WatchedDataChangedEvent(event: WatchedEvent) extends Event
  final case class WatchedNodeCreatedEvent(event: WatchedEvent) extends Event
  final case class WatchedNodeDeletedEvent(event: WatchedEvent) extends Event
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
  /**
   * Process an incoming [[org.apache.zookeeper.WatchedEvent]].
   * @param event event to process
   */
  override def process(event: WatchedEvent): Unit = {
    if (Option(event.getPath) != None) {
      event.getType() match {
        case EventType.NodeDeleted => {
          this.dataWatchEventHandler ! ZookeeperDataWatcher.WatchedNodeDeletedEvent(event)
        } 
        case EventType.NodeCreated => {
          this.dataWatchEventHandler ! ZookeeperDataWatcher.WatchedNodeCreatedEvent(event)
        } 
        case EventType.NodeDataChanged => {
          this.dataWatchEventHandler ! ZookeeperDataWatcher.WatchedDataChangedEvent(event)
        } 
        case EventType.NodeChildrenChanged => {
          this.dataWatchEventHandler ! ZookeeperDataWatcher.WatchedChildrenChangedEvent(event)
        }
      }
    }
  }
}