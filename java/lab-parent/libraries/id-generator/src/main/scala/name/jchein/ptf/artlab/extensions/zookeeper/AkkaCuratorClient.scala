package name.jchein.ptf.artlab.extensions.zookeeper

import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.x.async.AsyncCuratorFramework
import name.jchein.ptf.artlab.extensions.id_generator.IdGeneratorSettings
import java.time.temporal.ChronoUnit
import org.apache.curator.framework.state.ConnectionStateListenerManagerFactory
import org.apache.curator.framework.CuratorFramework

object AkkaCuratorClient {
  def apply(settings: ZkClientSettings) : AsyncCuratorFramework = {
    val retryPolicy = new ExponentialBackoffRetry(
      settings.baseRetryDelay.get(ChronoUnit.MILLIS).toInt,
      settings.maxRetryCount,
      settings.maxRetryDelay.get(ChronoUnit.MILLIS).toInt
    )
    val curatorBuilder = CuratorFrameworkFactory.builder()
      .connectString(settings.quorum)
      .retryPolicy(retryPolicy)
      .connectionStateListenerManagerFactory(
        ConnectionStateListenerManagerFactory.circuitBreaking(retryPolicy)
      ).canBeReadOnly(false)
      
    settings.authorization match {
      case Some((username, password)) => curatorBuilder.authorization(
          "digest", (username + ":" + password).getBytes())
      case None =>
    }

    val client: CuratorFramework = curatorBuilder.build()
    client.start()
    AsyncCuratorFramework.wrap(client);
  }
}
