package name.jchein.ptf.artlab.extensions.id_generator.zookeeper

import org.apache.curator.x.async.modeled.JacksonModelSerializer
import org.apache.curator.x.async.modeled.ModelSpec
import org.apache.curator.x.async.modeled.ModeledFramework
import org.apache.curator.x.async.modeled.ZPath
import org.apache.curator.x.async.AsyncCuratorFramework
import lombok.Value
import name.jchein.ptflibs.math.field.LongGroupGeneratorSpliterator
import name.jchein.ptflibs.math.field.PrimePowerGroup
import java.math.BigInteger
import java.math.BigInteger
import java.time.Duration
import org.apache.curator.x.async.modeled.ModelSerializer

case class LeaseRegistrationModel(val idName: String, val exp: Long) {
}

object LeaseRegistrationModel {
  private[zookeeper] val LEASE_REGISTRATION_SUFFIX: String = "leaseRegistration"

  def apply(
    async: AsyncCuratorFramework, znodePath: ZPath
  ): ModeledFramework[LeaseRegistrationModel] = {
    val mySer: ModelSerializer[LeaseRegistrationModel] =
      JacksonModelSerializer.build(classOf[LeaseRegistrationModel])
    val mySpec: ModelSpec[LeaseRegistrationModel] = ModelSpec.builder(mySer)
      .build()
    ModeledFramework.wrap(async, mySpec);
  }
}