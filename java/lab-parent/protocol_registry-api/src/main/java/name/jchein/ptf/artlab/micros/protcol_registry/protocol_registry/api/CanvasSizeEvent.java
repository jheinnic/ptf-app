package name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Preconditions;
import lombok.Value;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = CanvasSizeEvent.CanvasSizeCreated.class, name = "canvas-size-created"),
  @JsonSubTypes.Type(value = CanvasSizeEvent.CanvasSizeDisplayNameChanged.class, name = "canvas-size-display-name-changed")
})
public interface CanvasSizeEvent {

  String getName();

  @Value
  final class CanvasSizeCreated implements CanvasSizeEvent {
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

  @Value
  final class CanvasSizeDisplayNameChanged implements CanvasSizeEvent {
    public final String name;
    public final String displayName;

    @JsonCreator
    public CanvasSizeDisplayNameChanged(String name, String displayName) {
        this.name = Preconditions.checkNotNull(name, "name");
        this.displayName = Preconditions.checkNotNull(displayName, "displayName");
    }
  }
}
