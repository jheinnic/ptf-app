package name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.api;

import lombok.Value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;

@Value
@JsonDeserialize
public final class CanvasSizeCreatedReply {

	public final String name;

	@JsonCreator
	public CanvasSizeCreatedReply(String name) {
		this.name = Preconditions.checkNotNull(name, "name");
		Preconditions.checkArgument(! name.isEmpty(), "name");
	}
}
