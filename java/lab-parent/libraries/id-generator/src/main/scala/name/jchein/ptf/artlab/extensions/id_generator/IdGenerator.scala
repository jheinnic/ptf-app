package name.jchein.ptf.artlab.extensions.id_generator

import java.time.Clock

import org.pcollections.PSequence

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.pattern.StatusReply
import name.jchein.ptf.artlab.extensions.id_generator.zookeeper.ZookeeperULIDAuthority
import name.jchein.ptflibs.identity.ulid.ULID
import org.pcollections.TreePVector
import name.jchein.ptflibs.identity.ulid.ULIDRandomBitsStrategy
import name.jchein.ptflibs.identity.ulid.AbstractULIDRandomBitsStrategy
import name.jchein.ptflibs.identity.ulid.MonotonicULIDFactory
import scala.util.Failure
import scala.util.Success
import java.time.Instant
import java.time.Duration
import akka.util.Timeout

object IdGenerator {
  sealed trait Message
  sealed trait Request extends Message {
    val replyTo: ActorRef[StatusReply[Response]]
  }
  final case class OneUniqueIdRequest(replyTo: ActorRef[StatusReply[Response]]) extends Request
  final case class BulkUniqueIdsRequest(batchSize: Int, replyTo: ActorRef[StatusReply[Response]]) extends Request

  sealed trait Response
  final case class OneUniqueIdResponse(uniqueId: ULID) extends Response
  final case class BulkUniqueIdsResponse(uniqueIds: PSequence[ULID]) extends Response
  //  final case class GeneratorError(error: Exception) extends Response
  //  final case class InvalidRequest(error: String) extends Response

  val MAX_BATCH_SIZE = 32

  val Key: ServiceKey[Request] = ServiceKey(Constants.ID_GENERATOR_SERVICE_NAME)

  def apply(settings: IdGeneratorSettings): Behavior[IdGenerator.Message] = {
    val systemClock: Clock = Clock.systemUTC()
    val tickClock: Clock = Clock.tick(systemClock, settings.clockTickDuration)
    ////    val pseudoRandomness: ULIDRandomBitsStrategy =
    //    new AbstractULIDRandomBitsStrategy(0x9b9b9bL, 50, 2, 40, 24, 13);

    Behaviors.setup[IdGenerator.Message] { context ⇒
      Behaviors.withTimers { timers ⇒
        Behaviors.withStash(1000) { stash ⇒
          //          val zkEventAdapter: ActorRef[ZookeeperULIDAuthority.Event] =
          //            context.messageAdapter { event ⇒ WrappedZkEvent(event) }
          settings.sourceVariants.stream()
            .map { sourceConfig: SourceVariantConfigSettings ⇒
              sourceConfig match {
                case IdGeneratorSettings.ZookeeperLeaseConfigSettings ⇒ {
                  context.spawn(
                    ZkLeaseSourceVariant(sourceConfig),
                    s"zkLease_${sourceConfig.variantId}"
                  )
                }
                case Constants.VARIANT_SOURCE_SECURE_RANDOM_KIND ⇒ {
                  context.spawn(
                    SecureRandomSourceVariant(sourceConfig),
                    s"secureRandom_${sourceConfig.variantId}"
                  )
                }
              }
            }

          new IdGenerator(context, settings, zkActor, tickClock)
        }
      }
    }
  }
}

class IdGenerator(
  val context:   ActorContext[IdGenerator.Message],
  val settings:  IdGeneratorSettings,
  val zkActor:   ActorRef[ZookeeperClient.Query],
  val tickClock: Clock
) extends AbstractBehavior(context) {

  //          val renewalLeadTime: Duration = settings.IDGEN_RENEWAL_LEAD_TIME
  //          implicit val timeout: Timeout = settings.IDGEN_ACQUIRE_TIMEOUT

  def handleUnsubscribed(): Behavior[Message] = {
    context.askWithStatus(
      zkActor,
      { replyToRef: ActorRef[StatusReply[ZookeeperULIDAuthority.Response]] ⇒
        new ZookeeperULIDAuthority.SubscribeGeneratorClientRequest(replyToRef, zkEventAdapter)
      }
    ) {
        case Success(response: ZookeeperULIDAuthority.Response) ⇒ WrappedZkClientResponse(response)
        case Failure(exception: Throwable)                      ⇒ WrappedZkFatalError(exception)
      }

    Behaviors.receiveMessage {
      case WrappedZkClientResponse(response: ZookeeperSourceVariant.GeneratorULIDAuthoritySubscribedResponse) ⇒
        context.log.info(
          "ZK Client responded that it has processed subscription request.  Now we await events!"
        )
        Behaviors.same
      case WrappedZkClientResponse(response: ZookeeperSourceVariant.GeneratorULIDAuthorityUnsubscribedResponse) ⇒
        context.log.error(
          "ZK CLient responded with an unsolicitted unsubcribe response!"
        )
        Behaviors.unhandled
      case WrappedZkEvent(event: ZookeeperULIDAuthority.SessionConnectedEvent) ⇒
        context.log.info("ZK Client reports a session has been established!")
        handlePreClaim()
      case WrappedZkEvent(event: ZookeeperULIDAuthority.DroppedRequestEvent) ⇒
        context.log.error("ZK Client reports our request has been dropped?")
        handleUnsubscribed()
      case WrappedZkEvent(event: ZookeeperULIDAuthority.FailedToConnectEvent) ⇒
        // TODO: While we _do_ want to retry this, but with some delay...
        context.log.error("ZK Client reports a failure to bootstrap session with ZooKeeper")
        handleUnsubscribed()
      case WrappedZkEvent(event: ZookeeperULIDAuthority.SessionLostEvent) ⇒
        context.log.error("ZK Client reports a loss of a connection not yet acquired")
        Behaviors.unhandled
      case WrappedZkEvent(event: ZookeeperULIDAuthority.SessionSuspendedEvent) ⇒
        context.log.error("ZK Client reports a suspension of a connection not yet acquired")
        Behaviors.unhandled
      case WrappedZkEvent(event: ZookeeperULIDAuthority.SessionResumedEvent) ⇒
        context.log.error("ZK Client reports resumption of a session not yet connected")
        Behaviors.unhandled
      case WrappedZkEvent(event: ZookeeperULIDAuthority.ClaimedGeneratorIdEvent) ⇒
        context.log.error("ZK Client reports acquisition of a claim before we have a session")
        Behaviors.unhandled
      case WrappedZkEvent(event: ZookeeperULIDAuthority.TerminatedClaimEvent) ⇒
        context.log.error("ZK Client reports release of a claim we have not yet acquired")
        Behaviors.unhandled
      case WrappedZkFatalError(exception: Throwable) ⇒
        context.log.error(exception.getMessage)
        throw exception
      case others: IdGenerator.Request ⇒
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
      case WrappedZkEvent(event: ZookeeperULIDAuthority.SessionConnectedEvent) ⇒
        context.log.error("Received redundant connection message with session already established")
        Behaviors.unhandled
      case WrappedZkEvent(event: ZookeeperULIDAuthority.SessionLostEvent) ⇒
        // zkActor ! ZookeeperULIDAuthority.BootstrapZkRequest(zkClientResponseAdapter)
        handleUnsubscribed()
      case WrappedZkEvent(event: ZookeeperULIDAuthority.SessionSuspendedEvent) ⇒
        Behaviors.same
      case WrappedZkEvent(event: ZookeeperULIDAuthority.SessionResumedEvent) ⇒
        // TODO: This is not correct--we need to track whether we had a generator ID or not before we accept a signal that
        //       we have been :wrapped" o 223
        handlePreClaim()
      case WrappedZkEvent(event: ZookeeperULIDAuthority.ClaimedGeneratorIdEvent) ⇒
        stash.unstashAll(
          handlePostClaim(event.generatorId, event.expiresAfter)
        )
      case WrappedZkEvent(event: ZookeeperULIDAuthority.TerminatedClaimEvent) ⇒
        stash.unstashAll(
          handlePreClaim()
        )
      case WrappedZkClientResponse(response: ZookeeperULIDAuthority.GeneratorULIDAuthoritySubscribedResponse) ⇒
        Behaviors.unhandled
      case WrappedZkClientResponse(response: ZookeeperULIDAuthority.GeneratorULIDAuthorityUnsubscribedResponse) ⇒
        Behaviors.same
      case others: IdGenerator.Request ⇒
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
      case WrappedZkClientResponse(response: ZookeeperULIDAuthority.GeneratorULIDAuthoritySubscribedResponse) ⇒
        context.log.error("Received redundant response to subscription request")
        Behaviors.unhandled
      case WrappedZkClientResponse(response: ZookeeperULIDAuthority.GeneratorULIDAuthorityUnsubscribedResponse) ⇒
        context.log.error("Received unsolicited response to unsubscripion request")
        Behaviors.unhandled
      case WrappedZkEvent(event: ZookeeperULIDAuthority.ClaimedGeneratorIdEvent) ⇒
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
      case WrappedZkEvent(event: ZookeeperULIDAuthority.TerminatedClaimEvent) ⇒
        context.log.warn(
          s"Received notification about termination of lease on $leasedNodeId"
        );
        handlePreClaim()
      case OneUniqueIdRequest(replyTo: ActorRef[StatusReply[Response]]) ⇒
        try {
          val nextId: ULID = ulidFactory.nextULID
          context.log.info("Generating " + nextId.toString() + " on " + Thread.currentThread().getName())
          replyTo ! StatusReply.success(
            new OneUniqueIdResponse(nextId)
          )
        }
        catch {
          case e: Throwable ⇒ replyTo ! StatusReply.error(e)
        }
        Behaviors.same
      case BulkUniqueIdsRequest(batchSize: Int, replyTo: ActorRef[StatusReply[Response]]) ⇒
        if ((batchSize < 1) || (batchSize > MAX_BATCH_SIZE)) {
          replyTo ! StatusReply.error(
            new IllegalArgumentException(s"batchSize must be a positive value no greater than {MAX_BATCH_SIZE}")
          )
        }
        else {
          try {
            val idList = Range(0, batchSize)
              .map { idx ⇒ ulidFactory.nextULID }
              .foldLeft(TreePVector.empty[ULID]()) { (idList, ulid) ⇒ idList.plus(ulid) }
            replyTo ! StatusReply.success(
              new BulkUniqueIdsResponse(idList)
            )
          }
          catch {
            case e: Throwable ⇒ replyTo ! StatusReply.error(e)
          }
        }
        Behaviors.same
      case others: IdGenerator.Request ⇒
        others.replyTo ! StatusReply.error("Unexpeted request " + others.toString())
        Behaviors.unhandled
    }
  }

  handleUnsubscribed()
}