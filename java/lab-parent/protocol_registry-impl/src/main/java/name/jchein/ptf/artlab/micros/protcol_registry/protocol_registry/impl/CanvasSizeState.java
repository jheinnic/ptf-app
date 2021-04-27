package name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.impl;

import lombok.Value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.CompressedJsonable;

import java.time.LocalDateTime;

/**
 * The state for the {@link ProtocolRegistry} aggregate.
 */
@SuppressWarnings("serial")
@Value
@JsonDeserialize
public final class CanvasSizeState implements CompressedJsonable {
  public static final CanvasSizeState INITIAL = new CanvasSizeState("Hello", LocalDateTime.now().toString());
  public final String message;
  public final String timestamp;

  @JsonCreator
  public CanvasSizeState(String message, String timestamp) {
    this.message = Preconditions.checkNotNull(message, "message");
    this.timestamp = Preconditions.checkNotNull(timestamp, "timestamp");
  }

  public CanvasSizeState withMessage(String message) {
    return new CanvasSizeState(message, LocalDateTime.now().toString());
  }
}
