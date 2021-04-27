package name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.impl;

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
 * This interface defines all the events that the ProtocolRegistry aggregate supports.
 * <p>
 * By convention, the events should be inner classes of the interface, which
 * makes it simple to get a complete picture of what events an entity has.
 */
public interface CanvasSizeEvent extends Jsonable, AggregateEvent<CanvasSizeEvent> {

  /**
   * Tags are used for getting and publishing streams of events. Each event
   * will have this tag, and in this case, we are partitioning the tags into
   * 2 shards, which means we can have 2 concurrent processors/publishers of
   * events.
   */
  AggregateEventShards<CanvasSizeEvent> TAG = AggregateEventTag.sharded(CanvasSizeEvent.class, 2);

  /**
   * An event representing creation of a canvas size 
   */
  @SuppressWarnings("serial")
  @Value
  @JsonDeserialize
  public final class CanvasSizeCreated implements CanvasSizeEvent {

    public final String name;
    public final String displayName;
    public final int pixelWidth;
    public final int pixelHeight;
    public final int pixelUnit;

    @JsonCreator
    public CanvasSizeCreated(String name, String displayName, int pixelWidth, int pixelHeight, int pixelUnit) {
      this.name = Preconditions.checkNotNull(name, "name");
      this.displayName = Preconditions.checkNotNull(displayName, "displayName");
      this.pixelWidth = pixelWidth;
      this.pixelHeight = pixelHeight;
      this.pixelUnit = pixelUnit;
    }
  }

  @Override
  default AggregateEventTagger<CanvasSizeEvent> aggregateTag() {
    return TAG;
  }

}
