package name.jchein.ptf.artlab.micros.research_data.research_data.impl;

import lombok.Value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.CompressedJsonable;

import java.time.LocalDateTime;

/**
 * The state for the {@link ResearchData} aggregate.
 */
@SuppressWarnings("serial")
@Value
@JsonDeserialize
public final class ResearchDataState implements CompressedJsonable {
  public static final ResearchDataState INITIAL = new ResearchDataState("Hello", LocalDateTime.now().toString());
  public final String message;
  public final String timestamp;

  @JsonCreator
  public ResearchDataState(String message, String timestamp) {
    this.message = Preconditions.checkNotNull(message, "message");
    this.timestamp = Preconditions.checkNotNull(timestamp, "timestamp");
  }

  public ResearchDataState withMessage(String message) {
    return new ResearchDataState(message, LocalDateTime.now().toString());
  }
}
