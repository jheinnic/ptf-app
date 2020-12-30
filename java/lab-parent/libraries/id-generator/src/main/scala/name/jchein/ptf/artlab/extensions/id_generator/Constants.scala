package name.jchein.ptf.artlab.extensions.id_generator

import java.time.Duration
import akka.util.Timeout
import java.util.concurrent.TimeUnit

object Constants {
   val ID_GENERATOR_SERVICE_NAME: String = "idGenerator";

   val ZK_BLOCKING_DISPATCHER_NAME: String = "jchptf-zk-dispatcher";
   
   // Config root
   val CONFIG_PREFIX: String = "jchptf.idgen";
   
   // Local config keys
   val IDGEN_SEED_PRIME_POWER_KEY: String = "seed.primePower";
   
   val IDGEN_SEED_GENERATOR_KEY: String = "seed.generator";

   val IDGEN_SEED_BITS_FIXED_KEY: String = "seed.bitsFixed";
   
   val IDGEN_CLAIM_DURATION_KEY: String = "claim.duration";
   
   val IDGEN_CLAIM_DURATION_JITTER_KEY: String = "claim.durationJitter";
   
   val IDGEN_ACQUIRE_TIMEOUT_KEY: String = "claim.acquireTimeout";
   
   val IDGEN_RENEWAL_LEAD_TIME_KEY: String = "claim.renewalLeadTime"

   // Zookeeper config keys
   val ZK_QUORUM_KEY: String = "zookeeper.quorum"; 

   val ZK_USERNAME_KEY: String = "zookeeper.username"; 
   
   val ZK_PASSWORD_KEY: String = "zookeeper.password"; 
   
   val ZK_ZNODE_KEY: String = "zookeeper.znode";
   
   val ZK_BASE_RETRY_DELAY_KEY: String = "zookeeper.baseRetryDelay";
   
   val ZK_MAX_RETRY_DELAY_KEY: String = "zookeeper.maxRetryDelay";
   
   val ZK_MAX_RETRY_COUNT_KEY: String = "zookeeper.maxRetryCount";
   
   // ID Generation defaults
   val IDGEN_SEED_PRIME_POWER_DEFAULT: Long = 280651248517201L; // (4309 * 4309 * 4309 * 4309)

   val IDGEN_SEED_GENERATOR_DEFAULT: Long = 3701;
   
   val IDGEN_SEED_BITS_FIXED_DEFAULT: Int = 40;

   val IDGEN_SERIES_BITS_DEFAULT: Int = 24;

   val IDGEN_CONFLICT_BITS_DEFAULT: Int = 13;
 
   val IDGEN_CLAIM_DURATION_DEFAULT: Duration = Duration.ofSeconds(480);
   
   val IDGEN_CLAIM_DURATION_JITTER_DEFAULT: Double = 0.085
   
   val IDGEN_ACQUIRE_TIMEOUT_DEFAULT: Timeout = Timeout(20, TimeUnit.SECONDS);
   
   val IDGEN_RENEWAL_LEAD_TIME_DEFAULT: Duration = Duration.ofSeconds(40);
   
   // Zookeeper defaults
   val ZK_BASE_RETRY_DELAY_DEFAULT: Duration = Duration.ofMillis(800);
   
   val ZK_MAX_RETRY_DELAY_DEFAULT: Duration = Duration.ofSeconds(180);
   
   val ZK_MAX_RETRY_COUNT_DEFAULT: Int = 36;
}