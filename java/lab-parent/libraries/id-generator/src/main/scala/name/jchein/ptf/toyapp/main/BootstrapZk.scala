package name.jchein.ptf.toyapp.main

import org.apache.curator.x.async.AsyncCuratorFramework

import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import name.jchein.ptf.artlab.extensions.id_generator.IdGeneratorSettings
import name.jchein.ptf.artlab.extensions.id_generator.zookeeper.NodeSequenceModel
import name.jchein.ptf.artlab.extensions.zookeeper.AkkaCuratorClient
import name.jchein.ptf.artlab.extensions.zookeeper.ZkClientSettings
import name.jchein.ptf.artlab.extensions.id_generator.zookeeper.VariantConfigModel
import name.jchein.ptf.artlab.extensions.id_generator.Constants

object BootstrapZk extends App {
    ActorSystem[Nothing](BootstrapActor(), "BootstrapMe")  
}

object BootstrapActor {
  val PRIME: Long = 4309L;
  val POWER: Int = 4;
  val PRIME_POWER: Long = 280651248517201L; // (4309 * 4309 * 4309 * 4309)
  val GENERATOR: Long = 3701L;
    
  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { context: ActorContext[Nothing] =>
      val idSettings = IdGeneratorSettings(context.system)
      val zkSettings = ZkClientSettings(context.system)
      val zkIdSettings: IdGeneratorSettings.ZookeeperLeaseConfigSettings =
        idSettings.sourceVariants.get(0)
          .asInstanceOf[IdGeneratorSettings.ZookeeperLeaseConfigSettings]
      val client: AsyncCuratorFramework = AkkaCuratorClient(zkSettings)
      
      val model = NodeSequenceModel(
        client, 
        zkIdSettings.zpath.child(Constants.SEQUENCE_CHILD_NODE_NAME)
      )
      model.set(
        new NodeSequenceModel(
          zkIdSettings.variantId, 1L, zkIdSettings.generator
        )
      ).exceptionally { err => err.getMessage() }
      .thenAccept { value => System.out.println(value) }
      
      val config = VariantConfigModel.initFromProcessProps(zkIdSettings)
//      val model2 = VariantConfigModel(
//        client,
//        zkIdSettings.zpath.child(Constants.CONFIG_CHILD_NODE_NAME)
//      )
//      model.                                                                                                                                                                                                                                                                                                                                                         
      Behaviors.empty
  }
}