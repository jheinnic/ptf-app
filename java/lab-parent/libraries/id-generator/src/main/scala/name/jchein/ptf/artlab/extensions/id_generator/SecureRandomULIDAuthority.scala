package name.jchein.ptf.artlab.extensions.id_generator



import akka.actor.typed.Behavior
  import ULIDNodeAuthorityProtocol._
  import ULIDNodeAuthorityProtocol._
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import java.time.Duration
import akka.actor.typed.SupervisorStrategy
import ULIDNodeAuthorityProtocol._

object SecureRandomULIDProtocol {
  def apply(
    settings: IdGenerator.SecRandSettings
  ): Behavior[Message] = {
    Behaviors.supervise(
      Behaviors.setup[Message] { context: ActorContext[Message] ⇒
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
  context:  ActorContext[Message],
  settings: IdGeneratorSettings.SecureRandomConfigSettings
) extends AbstractBehavior[Message](context) {
	override def onMessage(msg: Message): Behavior[SecureRandomULIDAuthority.Message] = {
		this.context.log.info(s"Called onMessage($msg)")
		Behaviors.same
	}
}