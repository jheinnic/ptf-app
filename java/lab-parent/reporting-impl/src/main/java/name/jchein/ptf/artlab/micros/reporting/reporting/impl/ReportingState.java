package name.jchein.ptf.artlab.micros.reporting.reporting.impl;

import lombok.Value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.CompressedJsonable;

import java.time.LocalDateTime;

/**
 * The state for the {@link Reporting} aggregate.
 */
@SuppressWarnings("serial")
@Value
@JsonDeserialize
public final class ReportingState implements CompressedJsonable {
  public static final ReportingState INITIAL = new ReportingState("Hello", LocalDateTime.now().toString());
  public final String message;
  public final String timestamp;

  @JsonCreator
  public ReportingState(String message, String timestamp) {
    this.message = Preconditions.checkNotNull(message, "message");
    this.timestamp = Preconditions.checkNotNull(timestamp, "timestamp");
  }

  public ReportingState withMessage(String message) {
    return new ReportingState(message, LocalDateTime.now().toString());
  }
}
