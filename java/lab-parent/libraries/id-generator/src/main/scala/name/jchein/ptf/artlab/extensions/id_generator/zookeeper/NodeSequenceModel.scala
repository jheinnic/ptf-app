package name.jchein.ptf.artlab.extensions.id_generator.zookeeper

import com.google.common.collect.ImmutableSet
import java.math.BigInteger
import java.time.Duration
import lombok.Value

import org.apache.curator.x.async.api.CreateOption
import org.apache.curator.x.async.api.DeleteOption
import org.apache.curator.x.async.AsyncCuratorFramework
import org.apache.curator.x.async.modeled.JacksonModelSerializer
import org.apache.curator.x.async.modeled.ModelSpecBuilder
import org.apache.curator.x.async.modeled.ModelSpec
import org.apache.curator.x.async.modeled.ModelSerializer
import org.apache.curator.x.async.modeled.ModeledFramework
import org.apache.curator.x.async.modeled.ZPath

import name.jchein.ptflibs.math.field.LongGroupGeneratorSpliterator
import name.jchein.ptflibs.math.field.PrimePowerGroup

object NodeSequenceModel {
  def apply(
    async: AsyncCuratorFramework, znodePath: ZPath
  ): ModeledFramework[NodeSequenceModel] = {
    val mySer: ModelSerializer[NodeSequenceModel] = 
      JacksonModelSerializer.build(classOf[NodeSequenceModel])
    val mySpec: ModelSpec[NodeSequenceModel] = ModelSpec.builder(mySer)
      .withCreateOptions(
        ImmutableSet.of(CreateOption.createParentsAsContainers)
      ).withDeleteOptions(
        ImmutableSet.of(DeleteOption.guaranteed)
      ).build()
      //      znodePath.child(NODE_SEQUENCE_SUFFIX),
    ModeledFramework.wrap(async, mySpec);
  }
}