package name.jchein.ptf.artlab.extensions.id_generator

import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.List
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

import org.apache.curator.x.async.modeled.ZPath

import com.typesafe.config.Config

import akka.actor.typed.ActorSystem
import akka.actor.typed.receptionist.ServiceKey
import akka.util.Timeout
import name.jchein.ptf.artlab.extensions.id_generator.zookeeper.ZookeeperULIDAuthority

object IdGeneratorSettings {
  def apply (system: ActorSystem[_]) = {
    val zc = system.settings.config.getConfig(Constants.CONFIG_PREFIX)
   
    val IDGEN_THREAD_COUNT: Int = {
      if (zc.hasPath(Constants.IDGEN_THREAD_COUNT_KEY)) {
        zc.getInt(Constants.IDGEN_THREAD_COUNT_KEY)
      } else {
        Constants.IDGEN_THREAD_COUNT_DEFAULT
      }
    }
    
    val IDGEN_CLOCK_TICK_DURATION: Duration = {
      if (zc.hasPath(Constants.IDGEN_CLOCK_TICK_DURATION_KEY)) {
        zc.getDuration(Constants.IDGEN_CLOCK_TICK_DURATION_KEY)
      } else {
        Constants.IDGEN_CLOCK_TICK_DURATION_DEFAULT
      }
    }
  
    val IDGEN_SOURCE_VARIANT_ORDER: List[SourceVariantConfigSettings[_]] = {
      if (zc.hasPath(Constants.IDGEN_SOURCE_VARIANT_ORDER_KEY)) {
        zc.getConfigList(Constants.IDGEN_SOURCE_VARIANT_ORDER_KEY)
          .stream().map[SourceVariantConfigSettings[_]]{ sourceConfig: Config ⇒ 
          sourceConfig.getString(Constants.IDGEN_SOURCE_KIND_KEY) match {
            case Constants.VARIANT_SOURCE_ZOOKEEPER_LEASE_KIND ⇒ 
              ZookeeperLeaseConfigSettings(sourceConfig)
            case Constants.VARIANT_SOURCE_SECURE_RANDOM_KIND ⇒ 
              SecureRandomConfigSettings(sourceConfig)
          }
        }.collect(Collectors.toList[SourceVariantConfigSettings[_]]())
      }
      else {
        throw new IllegalStateException("Source variant config option is mandatory")
      }
    }
    
//    new IdGeneratorSettings(
//        IDGEN_THREAD_COUNT, 
//        IDGEN_CLOCK_TICK_DURATION,
//        IDGEN_SOURCE_VARIANT_ORDER
//    )
  }
  
  sealed trait SourceVariantConfigSettings[Svc] {
    val variantId: Byte
    val duration: Duration
    val jitter: Double
    val renewMargin: Duration
    val expireMargin: Duration
    val nodeBitCount: Int
    val epochBitCount: Int
    val seriesBitCount: Int
    val serviceKey: ServiceKey[Svc]
  }
  
  case class ZookeeperLeaseConfigSettings(
    val variantId: Byte, val duration: Duration, val jitter: Double,
    val renewMargin: Duration, val expireMargin: Duration,
    val nodeBitCount: Int, val epochBitCount: Int, val seriesBitCount: Int,
    val prime: Long, val exponent: Int, val primePower: Long,
    val generator: Long, val zpath: ZPath,
    val serviceKey: ServiceKey[ZookeeperULIDAuthority.Message]
  ) extends SourceVariantConfigSettings[ZookeeperULIDAuthority.Message]
      
  case class SecureRandomConfigSettings(
    val variantId: Byte, val duration: Duration, val jitter: Double,
    val renewMargin: Duration, val expireMargin: Duration,
    val nodeBitCount: Int, val epochBitCount: Int, val seriesBitCount: Int,
    val serviceKey: ServiceKey[SecureRandomULIDAuthority.Message],
  ) extends SourceVariantConfigSettings[SecureRandomULIDAuthority.Message]

  object ZookeeperLeaseConfigSettings {
    def apply(sourceConfig: Config) = {
      val IDGEN_VARIANT_ID_VALUE: Byte = 
        if (sourceConfig.hasPath(Constants.IDGEN_VARIANT_ID_VALUE_KEY)) 
          sourceConfig.getInt(Constants.IDGEN_VARIANT_ID_VALUE_KEY)
            .asInstanceOf[Byte]
        else 
          throw new IllegalArgumentException("Variant ID and Kind are mandatory")
    
      val CLAIM_ACQUIRE_TIMEOUT: Timeout = 
        if (sourceConfig.hasPath(Constants.CLAIM_ACQUIRE_TIMEOUT_KEY))
          Timeout(
            sourceConfig.getTemporal(Constants.CLAIM_ACQUIRE_TIMEOUT_KEY)
              .get(ChronoUnit.SECONDS),
            TimeUnit.SECONDS)
        else
          Constants.CLAIM_ACQUIRE_TIMEOUT_DEFAULT
    
      val SEED_PRIME_NUMBER: Long =
        if (sourceConfig.hasPath(Constants.SEED_PRIME_NUMBER_KEY))
          sourceConfig.getLong(Constants.SEED_PRIME_NUMBER_KEY)
        else
          Constants.SEED_PRIME_NUMBER_DEFAULT
        
      val SEED_EXPONENT_NUMBER: Int =
        if (sourceConfig.hasPath(Constants.SEED_EXPONENT_NUMBER_KEY))
          sourceConfig.getInt(Constants.SEED_EXPONENT_NUMBER_KEY)
        else
          Constants.SEED_EXPONENT_NUMBER_DEFAULT
        
      val SEED_PRIME_POWER_NUMBER: Long = 
        if (sourceConfig.hasPath(Constants.SEED_PRIME_POWER_NUMBER_KEY))
          sourceConfig.getLong(Constants.SEED_PRIME_POWER_NUMBER_KEY)
        else
          Constants.SEED_PRIME_POWER_NUMBER_DEFAULT
    
      val SEED_GENERATOR_NUMBER: Long = 
        if (sourceConfig.hasPath(Constants.SEED_GENERATOR_NUMBER_KEY))
          sourceConfig.getLong(Constants.SEED_GENERATOR_NUMBER_KEY)
        else
          Constants.SEED_GENERATOR_NUMBER_DEFAULT
    
      val SPEC_NODE_BIT_COUNT: Int =
        if (sourceConfig.hasPath(Constants.SPEC_NODE_BIT_COUNT_KEY)) {
          sourceConfig.getInt(Constants.SPEC_NODE_BIT_COUNT_KEY)
        } else {
          Constants.SPEC_NODE_BIT_COUNT_DEFAULT
        }
    
      val SPEC_EPOCH_BIT_COUNT: Int =
        if (sourceConfig.hasPath(Constants.SPEC_EPOCH_BIT_COUNT_KEY)) {
          sourceConfig.getInt(Constants.SPEC_EPOCH_BIT_COUNT_KEY)
        } else {
          Constants.SPEC_EPOCH_BIT_COUNT_DEFAULT
        }
    
      val SPEC_SERIES_BIT_COUNT: Int =
        if (sourceConfig.hasPath(Constants.SPEC_SERIES_BIT_COUNT_KEY)) {
          sourceConfig.getInt(Constants.SPEC_SERIES_BIT_COUNT_KEY)
        } else {
          Constants.SPEC_SERIES_BIT_COUNT_DEFAULT
        }
    
      val CLAIM_DURATION: Duration = 
        if (sourceConfig.hasPath(Constants.CLAIM_DURATION_KEY))
          Duration.ofSeconds(sourceConfig.getLong(Constants.CLAIM_DURATION_KEY))
        else
          Constants.CLAIM_DURATION_DEFAULT
    
      val CLAIM_JITTER: Double = 
        if (sourceConfig.hasPath(Constants.CLAIM_JITTER_KEY))
          sourceConfig.getDouble(Constants.CLAIM_JITTER_KEY)
        else
          Constants.CLAIM_JITTER_DEFAULT
    
      val CLAIM_RENEW_MARGIN: Duration = 
        if (sourceConfig.hasPath(Constants.CLAIM_RENEW_MARGIN_KEY))
          sourceConfig.getDuration(Constants.CLAIM_RENEW_MARGIN_KEY)
        else
          Constants.CLAIM_RENEW_MARGIN_DEFAULT
    
      val CLAIM_EXPIRE_MARGIN: Duration = 
        if (sourceConfig.hasPath(Constants.CLAIM_EXPIRE_MARGIN_KEY))
          sourceConfig.getDuration(Constants.CLAIM_EXPIRE_MARGIN_KEY)
        else
          Constants.CLAIM_EXPIRE_MARGIN_DEFAULT
      
      val ZK_ZNODE_ZPATH: ZPath = 
        if (sourceConfig.hasPath(Constants.ZK_ZNODE_ZPATH_KEY))
          ZPath.parse(sourceConfig.getString(Constants.ZK_ZNODE_ZPATH_KEY))
        else
          Constants.ZK_ZNODE_ZPATH_DEFAULT
      
      new IdGeneratorSettings.ZookeeperLeaseConfigSettings(
        IDGEN_VARIANT_ID_VALUE,
        CLAIM_DURATION, CLAIM_JITTER, CLAIM_RENEW_MARGIN, CLAIM_EXPIRE_MARGIN,
        SPEC_NODE_BIT_COUNT, SPEC_EPOCH_BIT_COUNT, SPEC_SERIES_BIT_COUNT,
        SEED_PRIME_NUMBER, SEED_EXPONENT_NUMBER, SEED_PRIME_POWER_NUMBER,
        SEED_GENERATOR_NUMBER, ZK_ZNODE_ZPATH,
        ServiceKey[ZookeeperULIDAuthority.Message](s"zkLeaseIdGen_${IDGEN_VARIANT_ID_VALUE}")
      );
    }
  }
    
  object SecureRandomConfigSettings {
    def apply(sourceConfig: Config) = {
      val IDGEN_VARIANT_ID_VALUE: Byte = 
        if (sourceConfig.hasPath(Constants.IDGEN_VARIANT_ID_VALUE_KEY)) 
          sourceConfig.getInt(Constants.IDGEN_VARIANT_ID_VALUE_KEY)
            .asInstanceOf[Byte]
        else 
          throw new IllegalArgumentException("Variant ID and Kind are mandatory")
  
      val SPEC_NODE_BIT_COUNT: Int =
        if (sourceConfig.hasPath(Constants.SPEC_NODE_BIT_COUNT_KEY)) 
          sourceConfig.getInt(Constants.SPEC_NODE_BIT_COUNT_KEY)
        else
          Constants.SPEC_NODE_BIT_COUNT_DEFAULT
    
      val SPEC_EPOCH_BIT_COUNT: Int =
        if (sourceConfig.hasPath(Constants.SPEC_EPOCH_BIT_COUNT_KEY)) 
          sourceConfig.getInt(Constants.SPEC_EPOCH_BIT_COUNT_KEY)
        else
          Constants.SPEC_EPOCH_BIT_COUNT_DEFAULT
    
      val SPEC_SERIES_BIT_COUNT: Int =
        if (sourceConfig.hasPath(Constants.SPEC_SERIES_BIT_COUNT_KEY)) 
          sourceConfig.getInt(Constants.SPEC_SERIES_BIT_COUNT_KEY)
        else
          Constants.SPEC_SERIES_BIT_COUNT_DEFAULT
    
      val CLAIM_DURATION: Duration = 
        if (sourceConfig.hasPath(Constants.CLAIM_DURATION_KEY))
          Duration.ofSeconds(sourceConfig.getLong(Constants.CLAIM_DURATION_KEY))
        else
          Constants.CLAIM_DURATION_DEFAULT
    
      val CLAIM_JITTER: Double = 
        if (sourceConfig.hasPath(Constants.CLAIM_JITTER_KEY))
          sourceConfig.getDouble(Constants.CLAIM_JITTER_KEY)
        else
          Constants.CLAIM_JITTER_DEFAULT
    
      val CLAIM_RENEW_MARGIN: Duration = 
        if (sourceConfig.hasPath(Constants.CLAIM_RENEW_MARGIN_KEY))
          sourceConfig.getDuration(Constants.CLAIM_RENEW_MARGIN_KEY)
        else
          Constants.CLAIM_RENEW_MARGIN_DEFAULT
    
      val CLAIM_EXPIRE_MARGIN: Duration = 
        if (sourceConfig.hasPath(Constants.CLAIM_EXPIRE_MARGIN_KEY))
          sourceConfig.getDuration(Constants.CLAIM_EXPIRE_MARGIN_KEY)
        else
          Constants.CLAIM_EXPIRE_MARGIN_DEFAULT
          
      new IdGeneratorSettings.SecureRandomConfigSettings(
        IDGEN_VARIANT_ID_VALUE,
        CLAIM_DURATION, CLAIM_JITTER, CLAIM_RENEW_MARGIN, CLAIM_EXPIRE_MARGIN,
        SPEC_NODE_BIT_COUNT, SPEC_EPOCH_BIT_COUNT, SPEC_SERIES_BIT_COUNT,
        ServiceKey[SecureRandomULIDAuthority.Message](s"secureRandomIdGen_${IDGEN_VARIANT_ID_VALUE}")
      )
    }
  }
}

case class IdGeneratorSettings(
    val threadCount: Int,
    val clockTickDuration: Duration,
    val sourceVariants: List[IdGeneratorSettings.SourceVariantConfigSettings[_]])
    