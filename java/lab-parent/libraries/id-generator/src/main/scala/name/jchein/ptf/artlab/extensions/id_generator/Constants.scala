package name.jchein.ptf.artlab.extensions.id_generator

import java.time.Duration
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import org.apache.curator.x.async.modeled.ZPath

object Constants {
   val ID_GENERATOR_SERVICE_NAME: String = "idGenerator";
   val ID_GENERATOR_ZK_LEASE_SERVICE_NAME: String = "idGeneratorZkLease";

   val CONFIG_CHILD_NODE_NAME: String = "configNode"
   val SEQUENCE_CHILD_NODE_NAME: String = "sequenceState"
   val LEADER_ROLE_CHILD_NODE_NAME: String = "leaderRole"
   val LEASE_REGISTRY_CHILD_NODE_NAME: String = "leaseRegistry"

   val ZK_BLOCKING_DISPATCHER_NAME: String = "jchptf-zk-dispatcher";
   
   // Config root
   val CONFIG_PREFIX: String = "jchptf.idgen";
   
   // Local config keys
   val IDGEN_CLOCK_TICK_DURATION_KEY: String = "tickDuration"
   
   val IDGEN_THREAD_COUNT_KEY: String = "concurrency"

   val IDGEN_SOURCE_VARIANT_ORDER_KEY = "origins"
   
   val IDGEN_VARIANT_ID_VALUE_KEY: String = "variantId"
   
   val IDGEN_SOURCE_KIND_KEY: String = "sourceKind"
   
   // Variant Types
   val VARIANT_SOURCE_ZOOKEEPER_LEASE_KIND = "zkLease"
   
   val VARIANT_SOURCE_SECURE_RANDOM_KIND = "secureRandom"
   
   // Common Source Type Properties
   val SPEC_NODE_BIT_COUNT_KEY: String = "spec.nodeBits";
 
   val SPEC_EPOCH_BIT_COUNT_KEY: String = "spec.epochBits";
   
   val SPEC_SERIES_BIT_COUNT_KEY: String = "spec.seriesBits";
   
   // ZK Lease Source Type Properties
   val SEED_PRIME_POWER_NUMBER_KEY: String = "seed.primePower";
   
   val SEED_PRIME_NUMBER_KEY: String = "seed.prime";
   
   val SEED_EXPONENT_NUMBER_KEY: String = "seed.power";

   val SEED_GENERATOR_NUMBER_KEY: String = "seed.generator";

   val CLAIM_ACQUIRE_TIMEOUT_KEY: String = "claim.acquireTimeout";

   val CLAIM_DURATION_KEY: String = "claim.duration";
   
   val CLAIM_JITTER_KEY: String = "claim.jitter";
   
   val CLAIM_RENEW_MARGIN_KEY: String = "claim.renewMargin"

   val CLAIM_EXPIRE_MARGIN_KEY: String = "claim.expireMargin"
   
   val ZK_ZNODE_ZPATH_KEY: String = "znode";

   // ID Generation defaults
   val SEED_PRIME_POWER_NUMBER_DEFAULT: Long = 280651248517201L; // (4309 * 4309 * 4309 * 4309)

   val SEED_PRIME_NUMBER_DEFAULT: Long = 4309L; // (4309 * 4309 * 4309 * 4309)

   val SEED_EXPONENT_NUMBER_DEFAULT: Int = 4;
   
   val SEED_GENERATOR_NUMBER_DEFAULT: Long = 3701L;
   
   val SPEC_NODE_BIT_COUNT_DEFAULT: Int = 40;

   val SPEC_EPOCH_BIT_COUNT_DEFAULT: Int = 24;

   val SPEC_SERIES_BIT_COUNT_DEFAULT: Int = 13;

   val CLAIM_ACQUIRE_TIMEOUT_DEFAULT: Timeout = Timeout(20, TimeUnit.SECONDS);
 
   val CLAIM_DURATION_DEFAULT: Duration = Duration.ofSeconds(480);
   
   val CLAIM_JITTER_DEFAULT: Double = 0.085
   
   val CLAIM_RENEW_MARGIN_DEFAULT: Duration = Duration.ofSeconds(20);
   
   val CLAIM_EXPIRE_MARGIN_DEFAULT: Duration = Duration.ofSeconds(5);
   
   val ZK_ZNODE_ZPATH_DEFAULT: ZPath = ZPath.parse("/jchptf/idgen/variants/0")
   
   // IdGen core defaults
   val IDGEN_CLOCK_TICK_DURATION_DEFAULT: Duration = Duration.ofMillis(100);
   
   val IDGEN_THREAD_COUNT_DEFAULT: Int = 2
}