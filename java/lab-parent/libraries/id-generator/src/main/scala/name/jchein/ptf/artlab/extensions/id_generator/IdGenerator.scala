package name.jchein.ptf.artlab.extensions.id_generator

import java.time.Clock
import java.time.Duration
import java.time.Instant

import scala.util.Failure
import scala.util.Success

import org.pcollections.TreePVector

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.scaladsl.Behaviors
import akka.pattern.StatusReply
import akka.util.Timeout
import name.jchein.ptf.artlab.extensions.id_generator.zookeeper.ZookeeperClient
import name.jchein.ptf.artlab.extensions.id_generator.zookeeper.ZookeeperEvents
import name.jchein.ptflibs.identity.ulid.AbstractULIDRandomBitsStrategy
//import name.jchein.ptf.artlab.extensions.ulid.ReservedBits
import name.jchein.ptflibs.identity.ulid.MonotonicULIDFactory
import name.jchein.ptflibs.identity.ulid.ULID
import name.jchein.ptflibs.identity.ulid.ULIDFactory
import name.jchein.ptflibs.identity.ulid.ULIDRandomBitsStrategy

object IdGenerator {
  sealed trait Message
  sealed trait Request extends Message {
    val replyTo: ActorRef[StatusReply[Response]]
  }
  final case class OneUniqueIdRequest(replyTo: ActorRef[StatusReply[Response]]) extends Request
  final case class BulkUniqueIdsRequest(batchSize: Int, replyTo: ActorRef[StatusReply[Response]]) extends Request
  
  sealed trait RequestResponse extends Message
  private[id_generator] final case class WrappedZkClientResponse(response: ZookeeperClient.Response) extends RequestResponse
  private[id_generator] final case class WrappedZkEvent(response: ZookeeperEvents.Event) extends RequestResponse
  private[id_generator] final case class WrappedZkFatalError(throwable: Throwable) extends RequestResponse

  sealed trait Response
  final case class OneUniqueIdResponse(uniqueId: ULID) extends Response
  final case class BulkUniqueIdsResponse(uniqueIds: TreePVector[ULID]) extends Response
  final case class GeneratorError(error: Exception) extends Response
  final case class InvalidRequest(error: String) extends Response
 
  val systemClock: Clock = Clock.systemUTC()
  val tickClock: Clock = Clock.tick(systemClock, Duration.ofMillis(500))
  val pseudoRandomness: ULIDRandomBitsStrategy =
    new AbstractULIDRandomBitsStrategy(0x9b9b9bL, 50, 2, 40, 24, 13);
//  val reservableRandomness: ReservedBits = new ReservedBits(1L, 2, 2)

  val MAX_BATCH_SIZE = 32

  val Key: ServiceKey[Request] = ServiceKey(Constants.ID_GENERATOR_SERVICE_NAME)

  def apply(settings: IdGeneratorSettings, zkActor: ActorRef[ZookeeperClient.Request]): Behavior[Message] = {
    Behaviors.setup[Message] { context =>
      Behaviors.withTimers { timers =>
        Behaviors.withStash(1000) { stash =>
          val zkClientResponseAdapter: ActorRef[ZookeeperClient.Response] =
            context.messageAdapter { response => WrappedZkClientResponse(response) }
          val zkEventAdapter: ActorRef[ZookeeperEvents.Event] =
            context.messageAdapter { event => WrappedZkEvent(event) }
          context.system.receptionist ! Receptionist.Register(Key, context.self)

          val renewalLeadTime: Duration = settings.IDGEN_RENEWAL_LEAD_TIME
          implicit val timeout: Timeout = settings.IDGEN_ACQUIRE_TIMEOUT

          def handlePreSession(): Behavior[Message] = {
            context.askWithStatus( zkActor,
              { ref: ActorRef[StatusReply[ZookeeperClient.Response]] => 
                new ZookeeperClient.SubscribeGeneratorClientRequest(ref, zkEventAdapter) }
            ) {
              case Success(response: ZookeeperClient.Response) => WrappedZkClientResponse(response)
              case Failure(exception: Throwable)               => WrappedZkFatalError(exception)
            }
  
            Behaviors.receiveMessage {
              case WrappedZkEvent(event: ZookeeperEvents.SessionConnectedEvent) =>
                handlePreClaim(false)
              case WrappedZkEvent(event: ZookeeperEvents.FailedToConnectEvent) =>
                // TODO: While we _do_ want to retry this, but with some delay...
                context.log.error(
                  "ZK Client reports a permanent connection failure error on bootstrapping to ZooKeeper")
                handlePreSession()
              case others: Request =>
                if (stash.isFull) {
                  others.replyTo ! StatusReply.error(
                    "Too many requests with bootstrap still pending")
                  Behaviors.unhandled
                } else {
                  stash.stash(others)
                  Behaviors.same
                }
            }
          }
  
          def handlePreClaim(postError: Boolean = false): Behavior[Message] =
            Behaviors.receiveMessage {
              case WrappedZkEvent(event: ZookeeperEvents.SessionConnectedEvent) =>
                Behaviors.same
              case WrappedZkEvent(event: ZookeeperEvents.SessionLostEvent) =>
                // zkActor ! ZookeeperClient.BootstrapZkRequest(zkClientResponseAdapter)
                handlePreClaim(false)
              case WrappedZkEvent(event: ZookeeperEvents.SessionSuspendedEvent) =>
                Behaviors.same
              case WrappedZkEvent(event: ZookeeperEvents.SessionResumedEvent) =>
                // TODO: This is not correct--we need to track whether we had a generator ID or not before we accept a signal that
                //       we have been :wrapped" o 223
                handlePreClaim(false)
              case WrappedZkEvent(event: ZookeeperEvents.ClaimedGeneratorIdEvent) =>
                stash.unstashAll(
                  initHandlePostClaim(event.generatorId, event.expiresAfter))
              case WrappedZkEvent(event: ZookeeperEvents.TerminatedClaimEvent) =>
                stash.unstashAll(
                  handlePreClaim(true))
              case WrappedZkClientResponse(response: ZookeeperClient.GeneratorClientSubscribedResponse) =>
                Behaviors.unhandled
              case WrappedZkClientResponse(response: ZookeeperClient.GeneratorClientUnsubscribedResponse) =>
                Behaviors.same
              case others: Request =>
                if (postError) {
                  others.replyTo ! StatusReply.error(
                    "ZooKeeper Client is failing to connect")
                  Behaviors.unhandled
                } else if (stash.isFull) {
                  others.replyTo ! StatusReply.error(
                    "Too many requests with bootstrap still pending")
                  Behaviors.unhandled
                } else {
                  stash.stash(others)
                  Behaviors.same
                }
            }
  
          def initHandlePostClaim(generatorId: Long, expiresAfter: Instant): Behavior[Message] = {
//              reservableRandomness.updateLocation(generatorId, settings.IDGEN_SEED_BITS_FIXED);
              val reservedRandomness = new AbstractULIDRandomBitsStrategy(
                  generatorId, 50, 2,
                  settings.IDGEN_SEED_BITS_FIXED,
                  settings.IDGEN_SERIES_BITS,
                  settings.IDGEN_CONFLICT_BITS);
              val ulidFactory = new MonotonicULIDFactory(tickClock, reservedRandomness)
              handlePostClaim(ulidFactory, generatorId, expiresAfter)
          }
              

          def handlePostClaim(generator: ULIDFactory, generatorId: Long, expiresAfter: Instant): Behavior[Message] =
            Behaviors.receiveMessage {
              case WrappedZkClientResponse(response: ZookeeperClient.GeneratorClientSubscribedResponse) =>
                Behaviors.same
              case WrappedZkClientResponse(response: ZookeeperClient.GeneratorClientUnsubscribedResponse) =>
                Behaviors.same
              case WrappedZkEvent(event: ZookeeperEvents.ClaimedGeneratorIdEvent) =>
                if (event.generatorId != generatorId) {
                    initHandlePostClaim(event.generatorId, event.expiresAfter);
                } else {
                    Behaviors.same;
                }
              case WrappedZkEvent(event: ZookeeperEvents.TerminatedClaimEvent) =>
                handlePreClaim(false)
              case OneUniqueIdRequest(replyTo: ActorRef[StatusReply[Response]]) =>
                try {
                  val nextId: ULID = generator.nextULID
                  context.log.info("Generating " + nextId.toString() + " on " + Thread.currentThread().getName())
                  replyTo ! StatusReply.success(
                    new OneUniqueIdResponse(nextId))
                } catch {
                  case e: Throwable => replyTo ! StatusReply.error(e)
                }
                Behaviors.same
              case BulkUniqueIdsRequest(batchSize: Int, replyTo: ActorRef[StatusReply[Response]]) =>
                if ((batchSize < 1) || (batchSize > MAX_BATCH_SIZE)) {
                  replyTo ! StatusReply.error(
                    new IllegalArgumentException(s"batchSize must be a positive value no greater than {MAX_BATCH_SIZE}"))
                } else {
                  try {
                    val idList = Range(0, batchSize)
                      .map { idx => generator.nextULID }
                      .foldLeft(TreePVector.empty[ULID]()) { (idList, ulid) => idList.plus(ulid) }
                    replyTo ! StatusReply.success(
                      new BulkUniqueIdsResponse(idList))
                  } catch {
                    case e: Throwable => replyTo ! StatusReply.error(e)
                  }
                }
                Behaviors.same
              case others: IdGenerator.Request =>
                others.replyTo ! StatusReply.error("Unexpeted request " + others.toString())
                Behaviors.unhandled
            }
  
          handlePreSession()
        }
      }
    }
  }
}

//object SystemClock extends Clock {
//  def currentTimeMillis(): Long = {
//    System.currentTimeMillis();
//  }
//}