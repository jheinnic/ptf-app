package name.jchein.ptf.artlab.micros.component_registry.impl;

import lombok.Value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.CompressedJsonable;

import java.time.LocalDateTime;

/**
 * The state for the {@link ComponentRegistry} aggregate.
 */
@SuppressWarnings("serial")
@Value
@JsonDeserialize
public final class ComponentRegistryState implements CompressedJsonable {
  public static final ComponentRegistryState INITIAL = new ComponentRegistryState("Hello", LocalDateTime.now().toString());
  public final String message;
  public final String timestamp;

  @JsonCreator
  public ComponentRegistryState(String message, String timestamp) {
    this.message = Preconditions.checkNotNull(message, "message");
    this.timestamp = Preconditions.checkNotNull(timestamp, "timestamp");
  }

  public ComponentRegistryState withMessage(String message) {
    return new ComponentRegistryState(message, LocalDateTime.now().toString());
  }
}
