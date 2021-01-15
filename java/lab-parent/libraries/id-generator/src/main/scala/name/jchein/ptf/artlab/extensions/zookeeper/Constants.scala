package name.jchein.ptf.artlab.extensions.zookeeper

import java.time.Duration
import akka.util.Timeout
import java.util.concurrent.TimeUnit

object Constants {
   val ZK_SERVICE_NAME: String = "zkClient";

   val ZK_BLOCKING_DISPATCHER_NAME: String = "jchptf-zk-dispatcher";
   
   // Config root
   val CONFIG_PREFIX: String = "jchptf.zookeeper";
    
   // Zookeeper config keys
   val ZK_QUORUM_KEY: String = "quorum"; 

   val ZK_USERNAME_KEY: String = "username"; 
   
   val ZK_PASSWORD_KEY: String = "password"; 
   
//   val ZK_ZNODE_KEY: String = "zookeeper.znode";
   
   val ZK_BASE_RETRY_DELAY_KEY: String = "baseRetryDelay";
   
   val ZK_MAX_RETRY_DELAY_KEY: String = "maxRetryDelay";
   
   val ZK_MAX_RETRY_COUNT_KEY: String = "maxRetryCount";
  
   // Zookeeper defaults
   val ZK_BASE_RETRY_DELAY_DEFAULT: Duration = Duration.ofMillis(800);
   
   val ZK_MAX_RETRY_DELAY_DEFAULT: Duration = Duration.ofSeconds(180);
   
   val ZK_MAX_RETRY_COUNT_DEFAULT: Int = 36;

   // ???
   val ULID_ZPATH_ACTOR_TYPE: String = "ulid"
}