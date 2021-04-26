package name.jchein.ptf.artlab.extensions.id_generator.zookeeper

import java.math.BigInteger
import java.time.Duration

import scala.util.Random
import akka.util.Timeout

import org.apache.curator.x.async.AsyncCuratorFramework
import org.apache.curator.x.async.modeled.JacksonModelSerializer
import org.apache.curator.x.async.modeled.ModelSpec
import org.apache.curator.x.async.modeled.ModeledFramework
import org.apache.curator.x.async.modeled.ZPath

import name.jchein.ptf.artlab.extensions.id_generator.Constants
import name.jchein.ptflibs.math.field.PrimePowerGroup
import name.jchein.ptflibs.math.field.LongGroupGeneratorSpliterator

class VariantConfigModel(
  val variant:             Byte,
  val p:                   Long,
  val n:                   Int,
  val pn:                  Long,
  val g:                   Long,
  val baseLeaseDuration:   Duration,
  val leaseDurationJitter: Double,
  val renewMargin:         Duration,
  val expireMargin:        Duration,
  val nodeBits:            Int,
  val epochBits:           Int,
  val seriesBits:          Int
) {
  def getGroupOrder(current: NodeSequenceModel): LongGroupGeneratorSpliterator = {
    if (current.variant != this.variant) {
      throw new IllegalArgumentException(
        s"Variant mismatch -- <${current.variant}> != <${this.variant}>"
      )
    }
    val ppg: PrimePowerGroup = PrimePowerGroup.getGroupByPrimePower(
      BigInteger.valueOf(p), n
    );
    return ppg.getPowerPrimeSequence(
      BigInteger.valueOf(g),
      BigInteger.valueOf(current.exp)
    );
  }

  def getNextId(current: NodeSequenceModel): NodeSequenceModel = {
    return this.getNextId(
      current,
      this.getGroupOrder(current)
    )
  }

  def getNextId(current: NodeSequenceModel, ord: LongGroupGeneratorSpliterator): NodeSequenceModel = {
    if ((ord.getG() != this.g) || (ord.getExponent() != current.exp) ||
      (ord.getP() != this.p) || (ord.getT() != current.t)) {
      throw new IllegalArgumentException("Generator does not match")
    }
    val retVal: Array[Long] = new Array[Long](4);
    if (
        !ord.tryAdvance { x:Long => retVal.update(0, x) } ||
        !ord.tryAdvance { x:Long => retVal.update(1, x) } ||
        !ord.tryAdvance { x:Long => retVal.update(2, x) } ||
        !ord.tryAdvance { x:Long => retVal.update(3, x) }
    ) {
      throw new IllegalStateException("Failed to advance GroupGenerator!");
    }

    return new NodeSequenceModel(current.variant, current.exp + 1, retVal(0));
  }

  def adjustForJitter(baseDuration: Duration, jitter: Double): Duration = {
    val variance: Long = Math.round(
      baseDuration.toMillis() *
        jitter *
        (0.5 - Random.nextDouble())
    );
    return baseDuration.plusMillis(variance);
  }
}

object VariantConfigModel {
  private[zookeeper] val CONFIG_PATH_SUFFIX: String = "configNode"

  def apply(async: AsyncCuratorFramework, settings: IdGeneratorSettings.ZookeeperLeaseConfigSettings, variant: Byte): ModeledFramework[VariantConfigModel] = {
    val mySpec: ModelSpec[VariantConfigModel] = ModelSpec.builder(
      settings.zpath.child(Constants.CONFIG_CHILD_NODE_NAME),
      JacksonModelSerializer.build(classOf[VariantConfigModel])
    )
      .build()
    ModeledFramework.wrap(async, mySpec);
  }

  def initFromProcessProps(
    config: IdGeneratorSettings.ZookeeperLeaseConfigSettings
  ): VariantConfigModel = new VariantConfigModel(
    config.variantId,
    config.prime,
    config.exponent,
    config.primePower,
    config.generator,
    config.duration,
    config.jitter,
    config.renewMargin,
    config.expireMargin,
    config.nodeBitCount,
    config.epochBitCount,
    config.seriesBitCount
  )
}