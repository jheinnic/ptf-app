package name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.api;

import lombok.Value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;

@Value
@JsonDeserialize
public final class GetCanvasSizeReply {

	public final String name;
	public final String displayName;
	public final int pixelWidth;
	public final int pixelHeight;
	public final int pixelUnit;

	@JsonCreator
	public GetCanvasSizeReply(String name, String displayName, int pixelWidth, int pixelHeight, int pixelUnit) {
		this.name = Preconditions.checkNotNull(name, "name");
		Preconditions.checkArgument(! name.isEmpty(), "name");
		this.displayName = Preconditions.checkNotNull(displayName, "displayName");
		Preconditions.checkArgument(! displayName.isEmpty(), "displayName");
		this.pixelWidth = pixelWidth;
		this.pixelHeight = pixelHeight;
		this.pixelUnit = pixelUnit;
	}
}
