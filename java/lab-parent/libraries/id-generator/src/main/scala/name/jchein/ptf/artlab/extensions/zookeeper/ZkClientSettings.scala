package name.jchein.ptf.artlab.extensions.zookeeper

import java.time.Duration

import scala.concurrent.Await
import scala.util.Try

import akka.actor.typed.ActorSystem
import com.typesafe.config.Config
import java.util.function.Supplier
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import java.util.ArrayList

object ZkClientSettings {
  def apply (system: ActorSystem[_]) = {
    val zc = system.settings.config.getConfig(Constants.CONFIG_PREFIX)
    val ZK_QUORUM = zc.getString(Constants.ZK_QUORUM_KEY);
    val ZK_AUTHORIZATION: Option[(String, String)] = {
      if (zc.hasPath(Constants.ZK_USERNAME_KEY) && zc.hasPath(Constants.ZK_PASSWORD_KEY)) {
        Some((zc.getString(Constants.ZK_USERNAME_KEY), zc.getString(Constants.ZK_PASSWORD_KEY)));
      }
      else {
        None
      }
    }
  
    val ZK_BASE_RETRY_DELAY: Duration = {
      if (zc.hasPath(Constants.ZK_BASE_RETRY_DELAY_KEY)) {
        Duration.ofSeconds(zc.getLong(Constants.ZK_BASE_RETRY_DELAY_KEY))
      }
      else {
        Constants.ZK_BASE_RETRY_DELAY_DEFAULT
      }
    }
  
    val ZK_MAX_RETRY_DELAY: Duration = {
      if (zc.hasPath(Constants.ZK_MAX_RETRY_DELAY_KEY)) {
        Duration.ofSeconds(zc.getLong(Constants.ZK_MAX_RETRY_DELAY_KEY))
      }
      else {
        Constants.ZK_MAX_RETRY_DELAY_DEFAULT
      }
    }
  
    val ZK_MAX_RETRY_COUNT: Int = {
      if (zc.hasPath(Constants.ZK_MAX_RETRY_COUNT_KEY)) {
        zc.getInt(Constants.ZK_MAX_RETRY_COUNT_KEY)
      }
      else {
        Constants.ZK_MAX_RETRY_COUNT_DEFAULT
      }
    }
    
    new ZkClientSettings(ZK_QUORUM, ZK_AUTHORIZATION, ZK_BASE_RETRY_DELAY,
        ZK_MAX_RETRY_DELAY, ZK_MAX_RETRY_COUNT)
  }
}

case class ZkClientSettings(
    val quorum: String,
    val authorization: Option[(String, String)], 
    val baseRetryDelay: Duration,
    val maxRetryDelay: Duration,
    val maxRetryCount: Int)