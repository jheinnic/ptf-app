package name.jchein.demo.lagom.hello_world.hello.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;

import lombok.Value;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = HelloEvent.GreetingMessageChanged.class, name = "greeting-message-changed") })
public interface HelloEvent extends AggregateEvent<HelloEvent> {

	/**
	 * Tags are used for getting and publishing streams of events. Each event will
	 * have this tag, and in this case, we are partitioning the tags into 4 shards,
	 * which means we can have 4 concurrent processors/publishers of events.
	 */
	AggregateEventShards<HelloEvent> TAG = AggregateEventTag.sharded(HelloEvent.class, 4);

	String getName();

	@Value
	final class GreetingMessageChanged implements HelloEvent {
		public final String name;
		public final String message;

		@JsonCreator
		public GreetingMessageChanged(String name, String message) {
			this.name = Preconditions.checkNotNull(name, "name");
			this.message = Preconditions.checkNotNull(message, "message");
		}
	}

	@Override
	default AggregateEventTagger<HelloEvent> aggregateTag() {
		return TAG;
	}
}
