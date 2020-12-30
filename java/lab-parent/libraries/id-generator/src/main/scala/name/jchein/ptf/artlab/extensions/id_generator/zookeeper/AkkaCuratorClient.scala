package name.jchein.ptf.artlab.extensions.id_generator.zookeeper

import akka.actor.typed.ActorRef

import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.x.async.AsyncCuratorFramework
import org.pcollections.{ PSequence, TreePVector }

import name.jchein.ptf.artlab.extensions.id_generator.IdGeneratorSettings

object AkkaCuratorClient {
  def apply(settings: IdGeneratorSettings) : AsyncCuratorFramework = {
    val curatorBuilder = CuratorFrameworkFactory.builder()
      .connectString(settings.ZK_QUORUM)
      .retryPolicy(
        new ExponentialBackoffRetry(
          settings.ZK_BASE_RETRY_DELAY.getSeconds().toInt,
          settings.ZK_MAX_RETRY_COUNT,
          settings.ZK_MAX_RETRY_DELAY.getSeconds().toInt
        )
      )
    settings.ZK_AUTHORIZATION match {
      case Some((username, password)) => curatorBuilder.authorization(
          "digest", (username + ":" + password).getBytes())
      case None =>
    }

    val client = curatorBuilder.build()

    client.start()
    AsyncCuratorFramework.wrap(client);
  }
}
