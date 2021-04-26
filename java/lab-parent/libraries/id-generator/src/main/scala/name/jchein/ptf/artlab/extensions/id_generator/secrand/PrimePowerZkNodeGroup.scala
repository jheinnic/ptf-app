package name.jchein.ptf.artlab.extensions.id_generator.secrand

import java.time.Duration
import java.time.Instant
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout
import name.jchein.ptf.artlab.extensions.id_generator.zookeeper.ZookeeperSourceVariant
import name.jchein.ptf.artlab.extensions.id_generator.zookeeper.ZookeeperSourceVariantEvents
import name.jchein.ptflibs.identity.ulid.AbstractULIDRandomBitsStrategy
import name.jchein.ptflibs.identity.ulid.MonotonicULIDFactory
import name.jchein.ptflibs.identity.ulid.ULIDRandomBitsStrategy
import akka.actor.typed.ActorRef.ActorRefOps
import name.jchein.ptf.artlab.extensions.id_generator.IdGenerator
import name.jchein.ptf.artlab.extensions.id_generator.IdGeneratorSettings
import name.jchein.ptf.artlab.extensions.id_generator.RandomBitsSourceVariant
import akka.actor.typed.receptionist.Receptionist
import name.jchein.ptf.artlab.extensions.id_generator.zookeeper.ZookeeperClient
import akka.pattern.StatusReply
import name.jchein.ptf.artlab.extensions.id_generator.RandomBitsSourceVariant
import name.jchein.ptf.artlab.extensions.id_generator.RandomBitsSourceVariant

object ZkLeaseSourceVariant extends RandomBitSequenceSource {
  sealed trait ZkAskResponse 
  private[variants] final case class WrappedZkClientResponse(response: ZookeeperSourceVariant.Response) extends ZkAskResponse
  private[variants] final case class WrappedZkEvent(response: ZookeeperSourceVariantEvents.Event) extends ZkAskResponse
//  private[id_generator] final case class WrappedZkFatalError(throwable: Throwable) extends ZkAskResponse

  def apply(
    settings: IdGeneratorSettings,
    parentActor: ActorRef[RandomBitsSourceVariant.NodeIdEvent],
    zkActor: ActorRef[ZookeeperSourceVariant.Request]
  ): Behavior[ZkAskResponse] = {
    Behaviors.setup[ZkAskResponse] { context ⇒
      context.system.receptionist ! Receptionist.Subscribe(ZookeeperClient.KEY)
      Behaviors.withTimers { timers ⇒
          val zkEventAdapter: ActorRef[ZookeeperSourceVariantEvents.Event] =
            context.messageAdapter { event ⇒ WrappedZkEvent(event) }

          val idClaimDuration: Duration = settings.IDGEN_CLAIM_DURATION
          val claimDurationJitter: Double = settings.IDGEN_CLAIM_DURATION_JITTER
          val renewalLeadTime: Duration = settings.IDGEN_RENEWAL_LEAD_TIME
          implicit val timeout: Timeout = settings.IDGEN_ACQUIRE_TIMEOUT

          def handleUnsubscribed(): Behavior[ZkAskResponse] = {
            context.askWithStatus(
              zkActor,
              { replyToRef: ActorRef[StatusReply[ZookeeperClient.Response]] ⇒
                new ZookeeperClient.SubscribeGeneratorClientRequest(replyToRef, zkEventAdapter) }
            ) {
                case Success(response: ZookeeperClient.Response) ⇒ WrappedZkClientResponse(response)
                case Failure(exception: Throwable)               ⇒ throw exception
              }
            
            Behaviors.receiveMessage {
              case WrappedZkClientResponse(response: ZookeeperSourceVariant.GeneratorClientSubscribedResponse) ⇒
                context.log.info(
                  "ZK Client responded that it has processed subscription request.  Now we await events!"
                )
                receivedReply = true
                Behaviors.same
              case WrappedZkClientResponse(response: ZookeeperSourceVariant.GeneratorClientUnsubscribedResponse) ⇒
                context.log.error(
                  "ZK Client responded with an unsolicitted unsubcribe response!"
                )
                Behaviors.unhandled
              case WrappedZkEvent(event: ZookeeperSourceVariantEvents.SessionConnectedEvent) ⇒
                context.log.info("ZK Client reports a session has been established!")
                handlePreClaim()
              case WrappedZkEvent(event: ZookeeperSourceVariantEvents.DroppedRequestEvent) ⇒
                context.log.error("ZK Client reports our request has been dropped?")
                handleUnsubscribed()
              case WrappedZkEvent(event: ZookeeperSourceVariantEvents.FailedToConnectEvent) ⇒
                // TODO: While we _do_ want to retry this, but with some delay...
                context.log.error("ZK Client reports a failure to bootstrap session with ZooKeeper")
                handleUnsubscribed()
              case WrappedZkEvent(event: ZookeeperSourceVariantEvents.SessionLostEvent) ⇒
                context.log.error("ZK Client reports a loss of a connection not yet acquired")
                Behaviors.unhandled
              case WrappedZkEvent(event: ZookeeperSourceVariantEvents.SessionSuspendedEvent) ⇒
                context.log.error("ZK Client reports a suspension of a connection not yet acquired")
                Behaviors.unhandled
              case WrappedZkEvent(event: ZookeeperSourceVariantEvents.SessionResumedEvent) ⇒
                context.log.error("ZK Client reports resumption of a session not yet connected")
                Behaviors.unhandled
              case WrappedZkEvent(event: ZookeeperSourceVariantEvents.ClaimedGeneratorIdEvent) ⇒
                context.log.error("ZK Client reports acquisition of a claim before we have a session")
                Behaviors.unhandled
              case WrappedZkEvent(event: ZookeeperSourceVariantEvents.TerminatedClaimEvent) ⇒
                context.log.error("ZK Client reports release of a claim we have not yet acquired")
                Behaviors.unhandled
              case WrappedZkFatalError(exception: Throwable) ⇒
                context.log.error(exception.getMessage)
                throw exception
              case others: Message ⇒
                if (stash.isFull) {
                  others.replyTo ! StatusReply.error(
                    "Too many requests with bootstrap still pending"
                  )
                  Behaviors.unhandled
                }
                else {
                  stash.stash(others)
                  Behaviors.same
                }
            }
          }

          def handlePreClaim(): Behavior[Message] =
            Behaviors.receiveMessage {
              case WrappedZkEvent(event: ZookeeperSourceVariantEvents.SessionConnectedEvent) ⇒
                context.log.error("Received redundant connection message with session already established")
                Behaviors.unhandled
              case WrappedZkEvent(event: ZookeeperSourceVariantEvents.SessionLostEvent) ⇒
                // zkActor ! ZookeeperClient.BootstrapZkRequest(zkClientResponseAdapter)
                handleUnsubscribed()
              case WrappedZkEvent(event: ZookeeperSourceVariantEvents.SessionSuspendedEvent) ⇒
                Behaviors.same
              case WrappedZkEvent(event: ZookeeperSourceVariantEvents.SessionResumedEvent) ⇒
                // TODO: This is not correct--we need to track whether we had a generator ID or not before we accept a signal that
                //       we have been :wrapped" o 223
                handlePreClaim()
              case WrappedZkEvent(event: ZookeeperSourceVariantEvents.ClaimedGeneratorIdEvent) ⇒
                stash.unstashAll(
                  handlePostClaim(event.generatorId, event.expiresAfter)
                )
              case WrappedZkEvent(event: ZookeeperSourceVariantEvents.TerminatedClaimEvent) ⇒
                stash.unstashAll(
                  handlePreClaim()
                )
              case WrappedZkClientResponse(response: ZookeeperSourceVariant.GeneratorClientSubscribedResponse) ⇒
                Behaviors.unhandled
              case WrappedZkClientResponse(response: ZookeeperSourceVariant.GeneratorClientUnsubscribedResponse) ⇒
                Behaviors.same
              case others: Message ⇒
                //                if (postError) {
                //                  others.replyTo ! StatusReply.error(
                //                    "ZooKeeper Client is failing to connect"
                //                  )
                //                  Behaviors.unhandled
                //                }
                if (stash.isFull) {
                  others.replyTo ! StatusReply.error(
                    "Too many requests with bootstrap still pending"
                  )
                  Behaviors.unhandled
                }
                else {
                  stash.stash(others)
                  Behaviors.same
                }
            }

          def handlePostClaim(leasedNodeId: Long, expiresAfter: Instant): Behavior[Message] = {
            // reservableRandomness.updateLocation(generatorId, settings.IDGEN_SEED_BITS_FIXED);
            val reservedRandomness: ULIDRandomBitsStrategy = new AbstractULIDRandomBitsStrategy(
              leasedNodeId, 50,
              settings.IDGEN_VARIANT_BITS_VALUE,
              settings.IDGEN_NODE_BIT_COUNT,
              settings.IDGEN_SERIES_BITS,
              settings.IDGEN_CONFLICT_BITS
            );
            val ulidFactory =
              new MonotonicULIDFactory(tickClock, reservedRandomness)
            Behaviors.receiveMessage {
              case WrappedZkClientResponse(response: ZookeeperSourceVariant.GeneratorClientSubscribedResponse) ⇒
                context.log.error("Received redundant response to subscription request")
                Behaviors.unhandled
              case WrappedZkClientResponse(response: ZookeeperSourceVariant.GeneratorClientUnsubscribedResponse) ⇒
                context.log.error("Received unsolicited response to unsubscripion request")
                Behaviors.unhandled
              case WrappedZkEvent(event: ZookeeperSourceVariantEvents.ClaimedGeneratorIdEvent) ⇒
                if (event.generatorId != leasedNodeId) {
                  context.log.info(
                    s"Received claim renewal updating node id from $leasedNodeId to ${event.generatorId}"
                  );
                  handlePostClaim(event.generatorId, event.expiresAfter);
                }
                else {
                  context.log.info(
                    s"Received claim renewal preserving node id as ${event.generatorId}"
                  );
                  Behaviors.same;
                }
              case WrappedZkEvent(event: ZookeeperSourceVariantEvents.TerminatedClaimEvent) ⇒
                context.log.warn(
                  s"Received notification about termination of lease on $leasedNodeId"
                );
                handlePreClaim()
              case others: IdGenerator.Request ⇒
                others.replyTo ! StatusReply.error("Unexpeted request " + others.toString())
                Behaviors.unhandled
            }
          }

          handleUnsubscribed()
      }
    }
  }
}

//object SystemClock extends Clock {
//  def currentTimeMillis(): Long = {
//    System.currentTimeMillis();
//  }
//}