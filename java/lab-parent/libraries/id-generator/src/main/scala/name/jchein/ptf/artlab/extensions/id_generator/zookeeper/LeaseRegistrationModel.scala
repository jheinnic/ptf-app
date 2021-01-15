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

case class LeaseRegistrationModel(val idName: String, val exp: Long) {
}

object LeaseRegistrationModel {
  private[zookeeper] val NODE_SEQUENCE_SUFFIX: String = "nodeSequence"

  def apply(
    async: AsyncCuratorFramework, znodePath: ZPath
  ): ModeledFramework[LeaseRegistrationModel] = {
    val mySpec: ModelSpec[LeaseRegistrationModel] = ModelSpec.builder(
      znodePath.child(NODE_SEQUENCE_SUFFIX),
      JacksonModelSerializer.build(classOf[LeaseRegistrationModel])
    ).build()
    ModeledFramework.wrap(async, mySpec);
  }
}