package name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.impl;

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

import name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.api.CreateCanvasSizeMessage;
import name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.api.ProtocolRegistryService;
import name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.impl.CanvasSizeCommand.*;

import javax.inject.Inject;
import java.time.Duration;

/**
 * Implementation of the ProtocolRegistryService.
 */
public class ProtocolRegistryServiceImpl implements ProtocolRegistryService {

	private final PersistentEntityRegistry persistentEntityRegistry;

	private final Duration askTimeout = Duration.ofSeconds(5);
	private ClusterSharding clusterSharding;

	@Inject
	public ProtocolRegistryServiceImpl(PersistentEntityRegistry persistentEntityRegistry,
			ClusterSharding clusterSharding) {
		this.clusterSharding = clusterSharding;
		// The persistent entity registry is only required to build an event stream for
		// the TopicProducer
		this.persistentEntityRegistry = persistentEntityRegistry;

		// register the Aggregate as a sharded entity
		this.clusterSharding
				.init(Entity.of(CanvasSizeAggregate.ENTITY_TYPE_KEY, CanvasSizeAggregate::create));
	}

	@Override
	public ServiceCall<NotUsed, String> hello(String id) {
		return request -> {

			// Look up the aggregete instance for the given ID.
			EntityRef<CanvasSizeCommand> ref = clusterSharding
					.entityRefFor(CanvasSizeAggregate.ENTITY_TYPE_KEY, id);
			// Ask the entity the Hello command.

			return ref.<CanvasSizeCommand.Greeting>ask(replyTo -> new Hello(id, replyTo), askTimeout)
					.thenApply(greeting -> greeting.message);
		};
	}

	@Override
	public ServiceCall<CreateCanvasSizeMessage, Done> useGreeting(String id) {
		return request -> {

			// Look up the aggregete instance for the given ID.
			EntityRef<CanvasSizeCommand> ref = clusterSharding
					.entityRefFor(CanvasSizeAggregate.ENTITY_TYPE_KEY, id);
			// Tell the entity to use the greeting message specified.

			return ref.<CanvasSizeCommand.Confirmation>ask(
					replyTo -> new UseGreetingMessage(request.message, replyTo), askTimeout).thenApply(confirmation -> {
						if (confirmation instanceof CanvasSizeCommand.Accepted) {
							return Done.getInstance();
						} else {
							throw new BadRequest(((CanvasSizeCommand.Rejected) confirmation).reason);
						}
					});
		};

	}

	@Override
	public Topic<name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.api.CanvasSizeEvent> helloEvents() {
		// We want to publish all the shards of the hello event
		return TopicProducer.taggedStreamWithOffset(CanvasSizeEvent.TAG.allTags(), (tag, offset) ->

		// Load the event stream for the passed in shard tag
		persistentEntityRegistry.eventStream(tag, offset).map(eventAndOffset -> {

			// Now we want to convert from the persisted event to the published event.
			// Although these two events are currently identical, in future they may
			// change and need to evolve separately, by separating them now we save
			// a lot of potential trouble in future.
			name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.api.CanvasSizeEvent eventToPublish;

			if (eventAndOffset.first() instanceof CanvasSizeEvent.GreetingMessageChanged) {
				CanvasSizeEvent.GreetingMessageChanged messageChanged = (CanvasSizeEvent.GreetingMessageChanged) eventAndOffset
						.first();
				eventToPublish = new name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.api.CanvasSizeEvent.GreetingMessageChanged(
						messageChanged.getName(), messageChanged.getMessage());
			} else {
				throw new IllegalArgumentException("Unknown event: " + eventAndOffset.first());
			}

			// We return a pair of the translated event, and its offset, so that
			// Lagom can track which offsets have been published.
			return Pair.create(eventToPublish, eventAndOffset.second());
		}));
	}
}
