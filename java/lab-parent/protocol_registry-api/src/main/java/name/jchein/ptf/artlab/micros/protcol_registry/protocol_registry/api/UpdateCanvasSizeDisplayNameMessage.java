package name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.api;

import lombok.Value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;

@Value
@JsonDeserialize
public final class UpdateCanvasSizeDisplayNameMessage {

	public final String displayName;

	@JsonCreator
	public UpdateCanvasSizeDisplayNameMessage(String displayName) {
		this.displayName = Preconditions.checkNotNull(displayName, "displayName");
		Preconditions.checkArgument(! displayName.isEmpty(), "displayName");
	}
}
