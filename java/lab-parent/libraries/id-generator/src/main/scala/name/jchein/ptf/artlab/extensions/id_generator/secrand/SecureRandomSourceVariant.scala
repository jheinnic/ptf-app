package name.jchein.ptf.artlab.extensions.id_generator.secrand

import name.jchein.ptf.artlab.extensions.id_generator.SecureRandomConfigSettings
import name.jchein.ptf.artlab.extensions.id_generator.IdGeneratorSettings
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.ActorContext

class SecureRandomSourceVariant(
  val context: ActorContext[Nothing],
  val srcConfig: SecureRandomConfigSettings,
  val config: IdGeneratorSettings
) extends AbstractBehavior[Nothing](context) {
   
}