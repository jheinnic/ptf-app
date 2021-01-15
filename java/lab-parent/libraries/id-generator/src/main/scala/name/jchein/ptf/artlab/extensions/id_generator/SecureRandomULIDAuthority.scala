package name.jchein.ptf.artlab.extensions.id_generator



import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import java.time.Duration
import akka.actor.typed.SupervisorStrategy

object SecureRandomULIDAuthority extends ULIDRandomBitsAuthority {
  def apply(
    settings: IdGeneratorSettings.SecureRandomConfigSettings
  ): Behavior[SecureRandomULIDAuthority.Message] = {
    Behaviors.supervise(
      Behaviors.setup[SecureRandomULIDAuthority.Message] { context: ActorContext[SecureRandomULIDAuthority.Message] ⇒
        context.log.info("In Zookeeper Client actor's setup handler")
        new SecureRandomULIDAuthority(context, settings)
      }
    ).onFailure[Throwable](
        SupervisorStrategy.restartWithBackoff(
          Duration.ofMillis(250), Duration.ofSeconds(45), 0.2
        )
      )
  }
}

class SecureRandomULIDAuthority(
  context:  ActorContext[SecureRandomULIDAuthority.Message],
  settings: IdGeneratorSettings.SecureRandomConfigSettings
) extends AbstractBehavior[SecureRandomULIDAuthority.Message](context) {
	override def onMessage(msg: SecureRandomULIDAuthority.Message): Behavior[SecureRandomULIDAuthority.Message] = {
		this.context.log.info(s"Called onMessage($msg)")
		Behaviors.same
	}
}