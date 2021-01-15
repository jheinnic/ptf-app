package name.jchein.ptf.artlab.extensions.zookeeper

import java.io.Closeable

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.{ immutable, mutable }
import scala.concurrent.duration.{ Duration, FiniteDuration }
import scala.concurrent.duration.Duration._
import scala.concurrent.duration.FiniteDuration._
import scala.util.Try
import scala.util.control.Exception.ignoring
import scala.util.Success

import akka.actor.typed.{ ActorRef, ActorSystem, ActorTags, Behavior, Extension, ExtensionId, Props, SupervisorStrategy }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }

import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.{ PathChildrenCacheEvent, PathChildrenCacheListener }
import org.apache.curator.framework.recipes.leader.{ LeaderLatch, LeaderLatchListener }
import org.apache.curator.framework.state.{ ConnectionState, ConnectionStateListener }
import org.apache.zookeeper.KeeperException.{ NoNodeException, NodeExistsException }

import java.util.concurrent.TimeUnit
import akka.actor.typed.scaladsl.Routers
import akka.actor.typed.Dispatchers
import akka.actor.typed.DispatcherSelector
import akka.actor.typed.receptionist.Receptionist

object ZookeeperClientExtension
extends ExtensionId[ZookeeperClientExtension] {
  // Scala API
  override def createExtension(system: ActorSystem[_]): ZookeeperClientExtension =
    new ZookeeperClientExtension(system)

  // Java API
  def get(system: ActorSystem[_]): ZookeeperClientExtension = apply(system)
}

class ZookeeperClientExtension(system: ActorSystem[_])
extends Extension {
  val settings = ZookeeperClientSettings(system)

  val zkActorRef = system.systemActorOf(
    Behaviors.supervise(
      ZookeeperClient(settings)
    ).onFailure(
      SupervisorStrategy.restartWithBackoff(
        minBackoff = FiniteDuration(2, TimeUnit.SECONDS),
        maxBackoff = FiniteDuration(5, TimeUnit.MINUTES),
        randomFactor = 0.2
      )
    ),
    "idGenZookeeperClient",
    ActorTags(
      Set("IdGenerator", "Zookeeeper")
    ).withDispatcherFromConfig(Constants.ZK_BLOCKING_DISPATCHER_NAME)
  );
  
  // make sure the workers are restarted if they fail

  system.receptionist ! Receptionist.Register(ZookeeperClient.Key, zkActorRef)
}
