package name.jchein.ptf.artlab.extensions.id_generator

import java.time.Clock
import java.time.Instant
import java.time.Duration

import org.apache.curator.x.async.modeled.ZPath
import org.pcollections.PSequence
import org.pcollections.TreePVector

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.pattern.StatusReply
import akka.pattern.CircuitBreaker
import akka.util.Timeout

import scala.util.Failure
import scala.util.Success

import name.jchein.ptflibs.identity.ulid.ULID
import name.jchein.ptflibs.identity.ulid.ULIDRandomBitsStrategy
import name.jchein.ptflibs.identity.ulid.AbstractULIDRandomBitsStrategy
import name.jchein.ptflibs.identity.ulid.MonotonicULIDFactory


object IdGenerator {
  sealed trait Message
  sealed trait Request extends Message {
    val replyTo: ActorRef[StatusReply[Response]]
  }
  final case class OneUniqueIdRequest(
    replyTo: ActorRef[StatusReply[OneUniqueIdResponse]]
  ) extends Request
  final case class BulkUniqueIdsRequest(
    batchSize: Int,
    replyTo: ActorRef[StatusReply[BulkUniqueIdsResponse]]
  ) extends Request

  sealed trait Response
  final case class OneUniqueIdResponse(uniqueId: ULID) extends Response
  final case class BulkUniqueIdsResponse(uniqueIds: PSequence[ULID]) extends Response
  
  sealed trait InternalMessage extends Message
  
  final case class WrappedListingsResponse(
    listingsResponse: Receptionist.Listing
  ) extends InternalMessage
  
  val MAX_BATCH_SIZE = 32

  val Key: ServiceKey[Request] = ServiceKey[Request](Constants.ID_GENERATOR_SERVICE_NAME)

  def apply(settings: IdGeneratorExtension.Settings): Behavior[IdGenerator.Message] = {
    val systemClock: Clock = Clock.systemUTC()
    val tickClock: Clock = Clock.tick(systemClock, settings.tickDuration)
    ////    val pseudoRandomness: ULIDRandomBitsStrategy =
    //    new AbstractULIDRandomBitsStrategy(0x9b9b9bL, 50, 2, 40, 24, 13);

    Behaviors.setup[IdGenerator.Message] { context ⇒
      Behaviors.withTimers { timers ⇒
        Behaviors.withStash(1000) { stash ⇒
          new IdGenerator(context, settings, stash, timers, tickClock)
        }
      }
    }
  }

  case class ZkNodeSequenceSpec(
    variantId: Byte, nodeBitCount: Int, 
    epochBitCount: Int, epochBitLimit: Int, 
    seriesBitCount: Int, zPath: ZPath,
    primePower: Long, generator: Long
  )
  case class ClaimPolicySettings(baseDuration: Duration, maxJitter: Duration, reservedLimit: Int)
  case class SecRandSettings(
      variantId: Byte, nodeBitCount: Int, epochBitCount: Int, epochBitLimit: Int, seriesBitCount: Int
  )
  case class LeaseHandlingSettings(renew: Duration, expire: Duration )
}

class IdGenerator(
  val context:   ActorContext[IdGenerator.Message],
  val settings:  IdGeneratorExtension.Settings,
  val stash: None,
  val timers: None,
  val tickClock: Clock
) extends AbstractBehavior(context) {
  import scala.concurrent.duration._
  import IdGenerator._
//  implicit val ec: scala.concurrent.ExecutionContext = 
//    scala.concurrent.ExecutionContext.global
//  val breaker =
//    new CircuitBreaker(context.system.classicSystem.scheduler, maxFailures = 5, callTimeout = 10.seconds, resetTimeout = 1.minute)
//      .onOpen(notifyMeOnOpen())
//  def notifyMeOnOpen(): Unit = { }

  def handleUnsubscribed(): Behavior[IdGenerator.Message] = {
//    context.askWithStatus(
//      zkActor,
//      { replyToRef: ActorRef[StatusReply[ZookeeperULIDAuthority.Response]] ⇒
//        new ZookeeperULIDAuthority.SubscribeGeneratorClientRequest(replyToRef, zkEventAdapter)
//      }
//    ) {
//        case Success(response: ZookeeperULIDAuthority.Response) ⇒ WrappedZkClientResponse(response)
//        case Failure(exception: Throwable)                      ⇒ WrappedZkFatalError(exception)
//      }

    Behaviors.receiveMessage {
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