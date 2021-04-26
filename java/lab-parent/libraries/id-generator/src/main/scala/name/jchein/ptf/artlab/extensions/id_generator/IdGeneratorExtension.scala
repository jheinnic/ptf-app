package name.jchein.ptf.artlab.extensions.id_generator

import java.time.Duration

import scala.collection.JavaConverters._
import scala.collection.immutable

import com.typesafe.config.Config

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Extension
import akka.actor.typed.ExtensionId
import akka.actor.typed.Props
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.ActorTags
import akka.actor.typed.DispatcherSelector
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.Routers
import akka.actor.typed.SupervisorStrategy


object IdGeneratorExtension extends ExtensionId[IdGeneratorExtension] with Extension {
  override def createExtension(system: ActorSystem[_]): IdGeneratorExtension =
    new IdGeneratorExtension(system)

  def get(system: ActorSystem[_]): IdGeneratorExtension = apply(system)
  
  trait OriginLocator {
    val id: String
    val key: ServiceKey[ULIDRandomBitsAuthority[_]]
  }
  trait Settings {
    val concurrency: Int
    val tickDuration: Duration
    val origins: List[OriginLocator]
  }
}

class IdGeneratorExtension(system: ActorSystem[_]) extends Extension {
  import IdGeneratorExtension._
  
  object settings extends Settings {
    private val genCfg: Config = system.settings.config.getConfig(Constants.CONFIG_PREFIX)
      
    val concurrency: Int = genCfg.getInt("concurrency")
    val tickDuration: Duration = genCfg.getDuration("tickDuration")
    val renewTolerance: Duration = genCfg.getDuration("renewTolerance")
    val origins: List[OriginLocator] = genCfg.getConfigList("origins")
      .asScala
      .toList
      .map { originConfig: Config => 
        object origin extends OriginLocator {
          val id: String = originConfig.getString("id")
          val key: ServiceKey[ULIDRandomBitsAuthority[_]] = 
            ServiceKey[ULIDRandomBitsAuthority[_]](id)
        }
        origin
      }
  }
  
  private val idGenPool: ActorRef[IdGenerator.Message] = system.systemActorOf(
    Routers.pool(poolSize = settings.concurrency)(
      Behaviors.supervise(
        IdGenerator(settings)
      ).onFailure[Exception](SupervisorStrategy.restart)
    ).withRouteeProps(
      DispatcherSelector.defaultDispatcher()
    ).withRoundRobinRouting(),
    Constants.ID_GENERATOR_SERVICE_NAME,
    ActorTags(
      "IdGenerator", "ServiceFacade", "Pool"
    ).withDispatcherDefault
  );
  
  system.receptionist ! Receptionist.Register(IdGenerator.Key, idGenPool)
}


// make sure the workers are restarted if they fail
//  settings.sourceVariants.forEach{
//    sourceConfig: IdGeneratorSettings.SourceVariantConfigSettings[_] =>
//      sourceConfig match {
//        case zkConfig: IdGeneratorSettings.ZookeeperLeaseConfigSettings => {
//          val sourceRef: ActorRef[ZookeeperULIDAuthority.Message] = system.systemActorOf(
//            Behaviors.supervise(
//              ZookeeperULIDAuthority(zkConfig)
//            ).onFailure[Exception](SupervisorStrategy.restart),
//            zkConfig.serviceKey.id,
//            ActorTags(
//              Set("IdGenerator", "ServiceFacade", "Pool")
//            ).withDispatcherDefault
//          )
//          system.receptionist ! Receptionist.Register(
//            zkConfig.serviceKey, sourceRef)
//        }
//        case secRandConfig: IdGeneratorSettings.SecureRandomConfigSettings => {
//          val sourceRef: ActorRef[SecureRandomULIDAuthority.Message] = system.systemActorOf(
//            Behaviors.supervise(
//              SecureRandomULIDAuthority(secRandConfig)
//            ).onFailure[Exception](SupervisorStrategy.restart),
//            secRandConfig.serviceKey.id,
//            ActorTags(
//              Set("IdGenerator", "ServiceFacade", "Pool")
//            ).withDispatcherDefault
//          )
//          system.receptionist ! Receptionist.Register(
//            sourceConfig.serviceKey, sourceRef)
//        }
//      }
//    }
