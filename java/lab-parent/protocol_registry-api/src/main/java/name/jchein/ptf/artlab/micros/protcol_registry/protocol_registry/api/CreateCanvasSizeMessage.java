package name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.api;

import lombok.Value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;

@Value
@JsonDeserialize
public final class CreateCanvasSizeMessage {

	public final String displayName;
	public final int pixelWidth;
	public final int pixelHeight;
	public final int pixelUnit;

	@JsonCreator
	public CreateCanvasSizeMessage(String displayName, int pixelWidth, int pixelHeight, int pixelUnit) {
		this.displayName = Preconditions.checkNotNull(displayName, "displayName");
		Preconditions.checkArgument(! displayName.isEmpty(), "displayName");
		this.pixelWidth = pixelWidth;
		this.pixelHeight = pixelHeight;
		this.pixelUnit = pixelUnit;
	}
}
