package name.jchein.ptf.artlab.micros.component_registry.impl;

import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import lombok.Value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.Jsonable;

/**
 * This interface defines all the events that the Hello aggregate supports.
 * <p>
 * By convention, the events should be inner classes of the interface, which
 * makes it simple to get a complete picture of what events an entity has.
 */
public interface ComponentRegistryEvent extends Jsonable, AggregateEvent<ComponentRegistryEvent> {

  /**
   * Tags are used for getting and publishing streams of events. Each event
   * will have this tag, and in this case, we are partitioning the tags into
   * 4 shards, which means we can have 4 concurrent processors/publishers of
   * events.
   */
  AggregateEventShards<ComponentRegistryEvent> TAG = AggregateEventTag.sharded(ComponentRegistryEvent.class, 4);

  /**
   * An event that represents a change in greeting message.
   */
  @SuppressWarnings("serial")
  @Value
  @JsonDeserialize
  public final class GreetingMessageChanged implements ComponentRegistryEvent {

    public final String name;
    public final String message;

    @JsonCreator
    public GreetingMessageChanged(String name, String message) {
      this.name = Preconditions.checkNotNull(name, "name");
      this.message = Preconditions.checkNotNull(message, "message");
    }
  }

  @Override
  default AggregateEventTagger<ComponentRegistryEvent> aggregateTag() {
    return TAG;
  }

}
