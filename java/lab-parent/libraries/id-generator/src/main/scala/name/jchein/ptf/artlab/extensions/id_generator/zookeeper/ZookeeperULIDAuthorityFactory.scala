package name.jchein.ptf.artlab.extensions.id_generator.zookeeper

import name.jchein.ptf.artlab.extensions.id_generator.IdGeneratorSettings
import akka.actor.typed.receptionist.Receptionist
import uk.co.appministry.akka.zk.ZkClientWatcher
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.receptionist.ServiceKey
import name.jchein.ptf.artlab.extensions.zookeeper.ZookeeperClient
import name.jchein.ptf.artlab.extensions.zookeeper.ZookeeperModelNode
import name.jchein.ptf.artlab.extensions.zookeeper.ZookeeperLeaderRecipe
import akka.actor.typed.ActorRef
import name.jchein.ptf.artlab.extensions.zookeeper.ZookeeperSessionWatcher
import akka.actor.typed.Behavior
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.StashBuffer
import org.apache.curator.x.async.modeled.versioned.Versioned

abstract class ZookeeperULIDAuthorityFactory(
	override val context: ActorContext[ZookeeperULIDAuthority.Message],
	val settings: IdGeneratorSettings.ZookeeperLeaseConfigSettings,
//	val sessionWatchEventHandler: ActorRef[ZookeeperSessionWatcher.Event]
)
extends AbstractBehavior(context)
with ZookeeperSessionWatcher {
  var stash: StashBuffer[ZookeeperULIDAuthority.Message]
  var zkSession: Option[ActorRef[ZookeeperClient.Message]]

  var zkLeaderKey: ServiceKey[ZookeeperLeaderRecipe.Message]
  var zkLeader: Option[ActorRef[ZookeeperLeaderRecipe.Message]]

  var zkConfigCacheKey: ServiceKey[ZookeeperModelNode.Message[VariantConfigModel]]
  var zkConfigCache: Option[ActorRef[ZookeeperModelNode.Message[VariantConfigModel]]]
  var configModel: Option[VariantConfigModel]

  var zkNodeSequenceKey: ServiceKey[ZookeeperModelNode.Message[NodeSequenceModel]]
  var zkNodeSequence: Option[ActorRef[ZookeeperModelNode.Message[NodeSequenceModel]]]
  var sequenceModel: Option[Versioned[NodeSequenceModel]]

  var zkLeaseRegisterKey: ServiceKey[ZookeeperModelNode.Message[LeaseRegistrationModel]]
  var zkLeaseRegister: Option[ActorRef[ZookeeperModelNode.Message[LeaseRegistrationModel]]]
  var registrations: Option[Array[LeaseRegistrationModel]]
  
  var dependenciesLeft: Int = 5

  context.system.receptionist ! Receptionist.Subscribe(
    ZookeeperClient.Key,
    context.messageAdapter(ZookeeperULIDAuthority.WrappedListing)
  )
	override def onMessage(msg: ZookeeperULIDAuthority.Message): Behavior[ZookeeperULIDAuthority.Message] = {
		this.context.log.info(s"Called ZookeeperULIDAuthorityFactory.onMessage($msg)")
		msg match {
		  case evt: ZookeeperULIDAuthority.WrappedListing => {
		    evt.event.getKey match {
		      case ZookeeperClient.Key => {
		        zkSession = Some(
		          evt.event.serviceInstances(ZookeeperClient.Key)
		            .iterator
		            .next
		        )
		      }
		    }
		  }
		}
		if(dependenciesLeft <= 0) {
//		  new ZookeeperULIDAuthority(
//		    context, settings, zkSession, zkLeader, 
//		    zkConfigCache, zkSeriesNode, zkRootChildNode)
		}
		this
	}
}
