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

import java.util.concurrent.TimeUnit
import akka.actor.typed.scaladsl.Routers
import akka.actor.typed.Dispatchers
import akka.actor.typed.DispatcherSelector
import akka.actor.typed.receptionist.Receptionist
import name.jchein.ptf.artlab.extensions.zookeeper.ZookeeperClient
import name.jchein.ptf.artlab.extensions.id_generator.zookeeper.ZookeeperULIDAuthority

object IdGeneratorExtension extends ExtensionId[IdGeneratorExtension] with Extension {

  def get(system: ActorSystem[_]): IdGeneratorExtension = apply(system)

  override def createExtension(system: ActorSystem[_]): IdGeneratorExtension =
    new IdGeneratorExtension(system)
}

class IdGeneratorExtension(system: ActorSystem[_]) extends Extension {
  val settings: IdGeneratorSettings = IdGeneratorSettings(system)

  // make sure the workers are restarted if they fail
  settings.sourceVariants.forEach{
    sourceConfig: IdGeneratorSettings.SourceVariantConfigSettings[_] =>
      sourceConfig match {
        case zkConfig: IdGeneratorSettings.ZookeeperLeaseConfigSettings => {
          val sourceRef: ActorRef[ZookeeperULIDAuthority.Message] = system.systemActorOf(
            Behaviors.supervise(
              ZookeeperULIDAuthority(zkConfig)
            ).onFailure[Exception](SupervisorStrategy.restart),
            zkConfig.serviceKey.id,
            ActorTags(
              Set("IdGenerator", "ServiceFacade", "Pool")
            ).withDispatcherDefault
          )
          system.receptionist ! Receptionist.Register(
            zkConfig.serviceKey, sourceRef)
        }
        case secRandConfig: IdGeneratorSettings.SecureRandomConfigSettings => {
          val sourceRef: ActorRef[SecureRandomULIDAuthority.Message] = system.systemActorOf(
            Behaviors.supervise(
              SecureRandomULIDAuthority(secRandConfig)
            ).onFailure[Exception](SupervisorStrategy.restart),
            secRandConfig.serviceKey.id,
            ActorTags(
              Set("IdGenerator", "ServiceFacade", "Pool")
            ).withDispatcherDefault
          )
          system.receptionist ! Receptionist.Register(
            sourceConfig.serviceKey, sourceRef)
        }
      }
    }

  val idGenPool: ActorRef[IdGenerator.Request] = system.systemActorOf(
    Routers.pool(poolSize = settings.threadCount)(
      Behaviors.supervise(
        IdGenerator(settings)
      ).onFailure[Exception](SupervisorStrategy.restart)
    ).withRouteeProps(DispatcherSelector.defaultDispatcher())
      .withRoundRobinRouting(),
    "idGenAppSvc",
    ActorTags(
      Set("IdGenerator", "ServiceFacade", "Pool")
    ).withDispatcherDefault
  );
  
  system.receptionist ! Receptionist.Register(IdGenerator.Key, idGenPool)
}
