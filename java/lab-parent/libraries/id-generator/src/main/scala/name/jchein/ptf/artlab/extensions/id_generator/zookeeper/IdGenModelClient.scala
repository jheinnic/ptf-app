package name.jchein.ptf.artlab.extensions.id_generator.zookeeper

import org.apache.curator.x.async.modeled.JacksonModelSerializer
import org.apache.curator.x.async.modeled.ModelSpec
import org.apache.curator.x.async.modeled.ModeledFramework
import org.apache.curator.x.async.modeled.ZPath
import org.apache.curator.x.async.AsyncCuratorFramework
import name.jchein.ptf.artlab.extensions.id_generator.IdGeneratorSettings
import lombok.Value

class IdGenConfig(p: Long, g: Long, i: Long, provisional: Boolean) {
  def getNextId(): IdGenConfig = new IdGenConfig(p, g, (g * i) % p, true)

  def getConfirmed(): IdGenConfig = new IdGenConfig(p, g, i, false)
  
  def getI(): Long = return i
}

object IdGenModelClient {
  private[zookeeper] val STATE_PATH_SUFFIX: String = "/stateNode"
  
  def apply(async: AsyncCuratorFramework, settings: IdGeneratorSettings): ModeledFramework[IdGenConfig] = {
    val mySpec: ModelSpec[IdGenConfig] = ModelSpec.builder(
      ZPath.parseWithIds(settings.ZK_ZNODE + STATE_PATH_SUFFIX),
      JacksonModelSerializer.build(classOf[IdGenConfig]))
      .build()
    ModeledFramework.wrap(async, mySpec);
  }
}

