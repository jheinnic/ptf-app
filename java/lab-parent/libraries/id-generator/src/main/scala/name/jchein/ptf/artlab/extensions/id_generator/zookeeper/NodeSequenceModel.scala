package name.jchein.ptf.artlab.extensions.id_generator.zookeeper

import org.apache.curator.x.async.modeled.JacksonModelSerializer
import org.apache.curator.x.async.modeled.ModelSpec
import org.apache.curator.x.async.modeled.ModeledFramework
import org.apache.curator.x.async.modeled.ZPath
import org.apache.curator.x.async.AsyncCuratorFramework
import name.jchein.ptf.artlab.extensions.id_generator.IdGeneratorSettings
import lombok.Value
import name.jchein.ptflibs.math.field.LongGroupGeneratorSpliterator
import name.jchein.ptflibs.math.field.PrimePowerGroup
import java.math.BigInteger
import java.math.BigInteger
import java.time.Duration

case class NodeSequenceModel(val variant: Byte, val exp: Long, val t: Long) {
}

object NodeSequenceModel {
  private[zookeeper] val NODE_SEQUENCE_SUFFIX: String = "nodeSequence"

  def apply(
    async: AsyncCuratorFramework, znodePath: ZPath
  ): ModeledFramework[NodeSequenceModel] = {
    val mySpec: ModelSpec[NodeSequenceModel] = ModelSpec.builder(
      znodePath.child(NODE_SEQUENCE_SUFFIX),
      JacksonModelSerializer.build(classOf[NodeSequenceModel])
    ).build()
    ModeledFramework.wrap(async, mySpec);
  }
}