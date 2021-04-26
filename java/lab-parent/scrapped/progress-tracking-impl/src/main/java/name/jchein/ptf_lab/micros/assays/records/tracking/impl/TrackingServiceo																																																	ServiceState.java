package name.jchein.ptf_lab.micros.assays.records.tracking.impl;

import lombok.Value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.CompressedJsonable;

import java.time.LocalDateTime;

/**
 * The state for the {@link TrackingServiceo																																																	Service} aggregate.
 */
@SuppressWarnings("serial")
@Value
@JsonDeserialize
public final class TrackingServiceo																																																	ServiceState implements CompressedJsonable {
  public static final TrackingServiceo																																																	ServiceState INITIAL = new TrackingServiceo																																																	ServiceState("Hello", LocalDateTime.now().toString());
  public final String message;
  public final String timestamp;

  @JsonCreator
  public TrackingServiceo																																																	ServiceState(String message, String timestamp) {
    this.message = Preconditions.checkNotNull(message, "message");
    this.timestamp = Preconditions.checkNotNull(timestamp, "timestamp");
  }

  public TrackingServiceo																																																	ServiceState withMessage(String message) {
    return new TrackingServiceo																																																	ServiceState(message, LocalDateTime.now().toString());
  }
}
