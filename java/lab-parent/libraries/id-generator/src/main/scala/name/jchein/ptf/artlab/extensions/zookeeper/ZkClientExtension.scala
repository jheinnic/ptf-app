package name.jchein.ptf.artlab.extensions.zookeeper

import java.io.Closeable

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.{ immutable, mutable }
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
import java.time.Duration
import com.typesafe.config.Config
import org.apache.curator.x.async.modeled.ModelSpec

object ZkClientExtension
extends ExtensionId[ZkClientExtension] {
  // Scala API
  override def createExtension(system: ActorSystem[_]): ZkClientExtension =
    new ZkClientExtension(system)

  // Java API
  def get(system: ActorSystem[_]): ZkClientExtension = apply(system)
  
  // type contract for content from Config metadata used to open a client Zookeeper session
  sealed trait Settings {
    val quorum: String
    val baseRetryDelay: Duration 
    val maxRetryDelay: Duration
    val maxRetryCount: Int 
    val retryRandomFactor: Double
    val authorization: Option[(String, String)]
  }
}

//    modelSpecs: Map[Class[_], List[ModelSpec[_]]]
class ZkClientExtension(system: ActorSystem[_])
extends Extension {
  val sessionsByName: mutable.Map[String, ActorRef[ZkClient]] =
    mutable.Map[String, ActorRef[ZkClient]]()

  def openZkSession(zkCfg: Config, adtorName: String) = {
    case object settings extends ZkClientExtension.Settings {
      // val actorName: String = zkCfg.getString("actorName")
      val quorum: String = zkCfg.getString("quorum")
      private val username: String = zkCfg.getString("username")
      private val password: String = zkCfg.getString("password")
      val baseRetryDelay: Duration = zkCfg.getDuration("baseRetryDelay")
      val maxRetryDelay: Duration = zkCfg.getDuration("maxRetryDelay")
      val maxRetryCount: Int = zkCfg.getInt("maxRetryCount")
      val retryRandomFactor: Double = zkCfg.getDouble("retryRandomFactor")
      val authorization: Option[(String, String)] =
        if (! (username.isEmpty || password.isEmpty)) {
          Some((username, password))
        } else {
          None
        }
    }
    import settings._
  
    val actorRef = system.systemActorOf(
      Behaviors.supervise(
        ZkClient(settings)
      ).onFailure(
        SupervisorStrategy.restartWithBackoff(
          minBackoff = baseRetryDelay,
          maxBackoff = maxRetryDelay,
          randomFactor = retryRandomFactor,
        )
      ),
      actorName,
      ActorTags(
        Set("IdGenerator", "Zookeeeper")
      ).withDispatcherFromConfig(Constants.ZK_BLOCKING_DISPATCHER_NAME)
    );
    
    sessionsByName + (actorName -> actorRef)
  }
}
