package name.jchein.ptf.artlab.extensions.id_generator

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

import name.jchein.ptf.artlab.extensions.id_generator.zookeeper.ZookeeperClient
import java.util.concurrent.TimeUnit

object IdGeneratorExtension extends ExtensionId[IdGeneratorExtension] with Extension {

  def get(system: ActorSystem[_]): IdGeneratorExtension = apply(system)

  override def createExtension(system: ActorSystem[_]): IdGeneratorExtension =
    new IdGeneratorExtension(system)
}

class IdGeneratorExtension(system: ActorSystem[_]) extends Extension {
  val settings = new IdGeneratorSettings(system)

  val zkActorRef = system.systemActorOf(
    Behaviors.supervise(
      Behaviors.setup[ZookeeperClient.Request](
        { context: ActorContext[ZookeeperClient.Request] =>
          context.log.info("In Zookeeper actor setup handler")
          ZookeeperClient(context, settings) }
      )
    ).onFailure(
      SupervisorStrategy.restartWithBackoff(
        minBackoff = FiniteDuration(2, TimeUnit.SECONDS),
        maxBackoff = FiniteDuration(5, TimeUnit.MINUTES),
        randomFactor = 0.2
      )
    ),
    "idGenZkClient",
    ActorTags(
      Set("IdGenerator", "Zookeeeper")
    ).withDispatcherFromConfig(Constants.ZK_BLOCKING_DISPATCHER_NAME)
  );
  system.systemActorOf(
    Behaviors.setup(
      { context: ActorContext[IdGenerator.Message] => IdGenerator(settings, zkActorRef) }
    ),
    "idGenAppSvc",
    ActorTags(
      Set("IdGenerator", "ServiceFacade")
    ).withDispatcherDefault
  );
}
