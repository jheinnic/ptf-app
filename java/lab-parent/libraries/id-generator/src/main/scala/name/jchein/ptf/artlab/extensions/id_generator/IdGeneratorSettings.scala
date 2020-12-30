package name.jchein.ptf.artlab.extensions.id_generator

import java.time.Duration

import scala.concurrent.Await
import scala.util.Try

import akka.actor.typed.ActorSystem
import com.typesafe.config.Config
import java.util.function.Supplier
import akka.util.Timeout
import java.util.concurrent.TimeUnit

class IdGeneratorSettings(system: ActorSystem[_]) {
  private val zc = system.settings.config.getConfig(Constants.CONFIG_PREFIX)

//  val USE_MODE = zc.getString(Constants.USE_MODE_KEY);
//  val LOCAL_CLUSTER_ID = zc.getString(Constants.LOCAL_CLUSTER_ID_KEY);
//  val LOCAL_GENERATOR_ID = zc.getString(Constants.LOCAL_GENERATOR_ID_KEY);

  val ZK_QUORUM = zc.getString(Constants.ZK_QUORUM_KEY);
  val ZK_ZNODE = zc.getString(Constants.ZK_ZNODE_KEY);
  val ZK_AUTHORIZATION: Option[(String, String)] =
    if (zc.hasPath(Constants.ZK_USERNAME_KEY) && zc.hasPath(Constants.ZK_PASSWORD_KEY))
      Some((zc.getString(Constants.ZK_USERNAME_KEY), zc.getString(Constants.ZK_PASSWORD_KEY)));
    else None

  val IDGEN_SEED_PRIME_POWER: Long = {
    if (zc.hasPath(Constants.IDGEN_SEED_PRIME_POWER_KEY)) 
      zc.getLong(Constants.IDGEN_SEED_PRIME_POWER_KEY)
    else
      Constants.IDGEN_SEED_PRIME_POWER_DEFAULT
    }

  val IDGEN_SEED_GENERATOR: Long = {
    if (zc.hasPath(Constants.IDGEN_SEED_GENERATOR_KEY)) 
      zc.getLong(Constants.IDGEN_SEED_GENERATOR_KEY)
    else
      Constants.IDGEN_SEED_GENERATOR_DEFAULT
    }

  val IDGEN_SEED_BITS_FIXED: Int = {
    if (zc.hasPath(Constants.IDGEN_SEED_BITS_FIXED_KEY)) 
      zc.getInt(Constants.IDGEN_SEED_BITS_FIXED_KEY)
    else
      Constants.IDGEN_SEED_BITS_FIXED_DEFAULT
    }
  
  val IDGEN_SERIES_BITS: Int = {
    Constants.IDGEN_SERIES_BITS_DEFAULT
  }

  val IDGEN_CONFLICT_BITS: Int = {
    Constants.IDGEN_CONFLICT_BITS_DEFAULT
  }

  val IDGEN_CLAIM_DURATION: Duration = {
    if (zc.hasPath(Constants.IDGEN_CLAIM_DURATION_KEY)) 
      Duration.ofSeconds(zc.getLong(Constants.IDGEN_CLAIM_DURATION_KEY))
    else
      Constants.IDGEN_CLAIM_DURATION_DEFAULT
    }

  val IDGEN_CLAIM_DURATION_JITTER: Double = {
    if (zc.hasPath(Constants.IDGEN_CLAIM_DURATION_JITTER_KEY)) 
      zc.getDouble(Constants.IDGEN_CLAIM_DURATION_JITTER_KEY)
    else
      Constants.IDGEN_CLAIM_DURATION_JITTER_DEFAULT
    }

  val IDGEN_ACQUIRE_TIMEOUT: Timeout = {
    if (zc.hasPath(Constants.IDGEN_ACQUIRE_TIMEOUT_KEY)) 
       Timeout(zc.getLong(Constants.IDGEN_ACQUIRE_TIMEOUT_KEY), TimeUnit.SECONDS)
    else
       Constants.IDGEN_ACQUIRE_TIMEOUT_DEFAULT
    }

  val IDGEN_RENEWAL_LEAD_TIME: Duration = {
    if (zc.hasPath(Constants.IDGEN_RENEWAL_LEAD_TIME_KEY)) 
      Duration.ofSeconds(zc.getLong(Constants.IDGEN_RENEWAL_LEAD_TIME_KEY))
    else
      Constants.IDGEN_RENEWAL_LEAD_TIME_DEFAULT
    }
  
  val ZK_BASE_RETRY_DELAY: Duration = if (zc.hasPath(Constants.ZK_BASE_RETRY_DELAY_KEY)) {
    Duration.ofSeconds(zc.getLong(Constants.ZK_BASE_RETRY_DELAY_KEY))
  } else {
    Constants.ZK_BASE_RETRY_DELAY_DEFAULT
  }
      
  val ZK_MAX_RETRY_DELAY: Duration = if (zc.hasPath(Constants.ZK_MAX_RETRY_DELAY_KEY)) {
    Duration.ofSeconds(zc.getLong(Constants.ZK_MAX_RETRY_DELAY_KEY))
  } else {
    Constants.ZK_MAX_RETRY_DELAY_DEFAULT
  }
      
  val ZK_MAX_RETRY_COUNT: Int = if (zc.hasPath(Constants.ZK_MAX_RETRY_COUNT_KEY)) {
    zc.getInt(Constants.ZK_MAX_RETRY_COUNT_KEY)
  } else {
    Constants.ZK_MAX_RETRY_COUNT_DEFAULT
  }
}

//object IdGenUseModes {
//  val spread = "spread"
//  val timed = "timed"
//}