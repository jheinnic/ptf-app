package name.jchein.ptf.artlab.extensions.zookeeper

import org.apache.zookeeper.WatchedEvent
import org.apache.zookeeper.Watcher.Event.EventType

/**
  * A rich wrapper for the [[org.apache.zookeeper.WatchedEvent]]
  * @param underlying original event
  */
case class WatchedEventMeta(val underlying: WatchedEvent) {
  /**
    * List of data change event types.
    */
  val dataChangeTriggeringEvents = List(
    EventType.NodeDataChanged,
    EventType.NodeDeleted,
    EventType.NodeCreated )

  /**
    * List of child change event types.
    */
  val childChangeTriggeringEvents = List(
    EventType.NodeChildrenChanged,
    EventType.NodeCreated,
    EventType.NodeDeleted )

  /**
    * State changed?.
    */
  lazy val stateChanged = Option(underlying.getPath) == None

  /**
    * znode changed?.
    */
  lazy val znodeChanged = Option(underlying.getPath) != None

  /**
    * Data changed?.
    */
  lazy val dataChanged = dataChangeTriggeringEvents.contains(underlying.getType)

  /**
    * Children changed?.
    */
  lazy val childrenChanged = childChangeTriggeringEvents.contains(underlying.getType)
}

