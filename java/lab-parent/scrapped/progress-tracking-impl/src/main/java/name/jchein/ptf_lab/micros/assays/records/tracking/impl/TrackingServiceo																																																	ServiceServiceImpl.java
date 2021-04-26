package name.jchein.ptf_lab.micros.assays.records.tracking.impl;

import akka.Done;
import akka.NotUsed;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.japi.Pair;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.transport.BadRequest;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import name.jchein.ptf_lab.micros.assays.records.tracking.api.GreetingMessage;
import name.jchein.ptf_lab.micros.assays.records.tracking.api.TrackingServiceo																																																	ServiceService;
import name.jchein.ptf_lab.micros.assays.records.tracking.impl.TrackingServiceo																																																	ServiceCommand.*;

import javax.inject.Inject;
import java.time.Duration;

/**
 * Implementation of the TrackingServiceo																																																	ServiceService.
 */
public class TrackingServiceo																																																	ServiceServiceImpl implements TrackingServiceo																																																	ServiceService {

  private final PersistentEntityRegistry persistentEntityRegistry;

  private final Duration askTimeout = Duration.ofSeconds(5);
  private ClusterSharding clusterSharding;

  @Inject
  public TrackingServiceo																																																	ServiceServiceImpl(PersistentEntityRegistry persistentEntityRegistry, ClusterSharding clusterSharding){
    this.clusterSharding=clusterSharding;
    // The persistent entity registry is only required to build an event stream for the TopicProducer
    this.persistentEntityRegistry=persistentEntityRegistry;

    // register the Aggregate as a sharded entity
    this.clusterSharding.init(
    Entity.of(
    TrackingServiceo																																																	ServiceAggregate.ENTITY_TYPE_KEY,
    TrackingServiceo																																																	ServiceAggregate::create
    )
    );
  }

  @Override
  public ServiceCall<NotUsed, String> hello(String id) {
    return request -> {

    // Look up the aggregete instance for the given ID.
    EntityRef<TrackingServiceo																																																	ServiceCommand> ref = clusterSharding.entityRefFor(TrackingServiceo																																																	ServiceAggregate.ENTITY_TYPE_KEY, id);
    // Ask the entity the Hello command.

    return ref.
      <TrackingServiceo																																																	ServiceCommand.Greeting>ask(replyTo -> new Hello(id, replyTo), askTimeout)
      .thenApply(greeting -> greeting.message);    };
  }

  @Override
  public ServiceCall<GreetingMessage, Done> useGreeting(String id) {
    return request -> {

    // Look up the aggregete instance for the given ID.
    EntityRef<TrackingServiceo																																																	ServiceCommand> ref = clusterSharding.entityRefFor(TrackingServiceo																																																	ServiceAggregate.ENTITY_TYPE_KEY, id);
    // Tell the entity to use the greeting message specified.

    return ref.
      <TrackingServiceo																																																	ServiceCommand.Confirmation>ask(replyTo -> new UseGreetingMessage(request.message, replyTo), askTimeout)
          .thenApply(confirmation -> {
              if (confirmation instanceof TrackingServiceo																																																	ServiceCommand.Accepted) {
                return Done.getInstance();
              } else {
                throw new BadRequest(((TrackingServiceo																																																	ServiceCommand.Rejected) confirmation).reason);
              }
          });
    };

  }

  @Override
  public Topic<name.jchein.ptf_lab.micros.assays.records.tracking.api.TrackingServiceo																																																	ServiceEvent> helloEvents() {
    // We want to publish all the shards of the hello event
    return TopicProducer.taggedStreamWithOffset(TrackingServiceo																																																	ServiceEvent.TAG.allTags(), (tag, offset) ->

      // Load the event stream for the passed in shard tag
      persistentEntityRegistry.eventStream(tag, offset).map(eventAndOffset -> {

      // Now we want to convert from the persisted event to the published event.
      // Although these two events are currently identical, in future they may
      // change and need to evolve separately, by separating them now we save
      // a lot of potential trouble in future.
      name.jchein.ptf_lab.micros.assays.records.tracking.api.TrackingServiceo																																																	ServiceEvent eventToPublish;

      if (eventAndOffset.first() instanceof TrackingServiceo																																																	ServiceEvent.GreetingMessageChanged) {
        TrackingServiceo																																																	ServiceEvent.GreetingMessageChanged messageChanged = (TrackingServiceo																																																	ServiceEvent.GreetingMessageChanged) eventAndOffset.first();
        eventToPublish = new name.jchein.ptf_lab.micros.assays.records.tracking.api.TrackingServiceo																																																	ServiceEvent.GreetingMessageChanged(
          messageChanged.getName(), messageChanged.getMessage()
        );
      } else {
        throw new IllegalArgumentException("Unknown event: " + eventAndOffset.first());
      }

        // We return a pair of the translated event, and its offset, so that
        // Lagom can track which offsets have been published.
        return Pair.create(eventToPublish, eventAndOffset.second());
      })
    );
  }
}
