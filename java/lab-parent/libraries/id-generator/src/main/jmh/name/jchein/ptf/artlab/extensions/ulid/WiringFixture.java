package name.jchein.ptf.artlab.extensions.ulid;

import java.time.Clock;

import name.jchein.ptflibs.identity.ulid.MonotonicULIDFactory;
import name.jchein.ptflibs.identity.ulid.ULIDRandomBitsStrategy;
import name.jchein.ptflibs.identity.ulid.SimpleULIDFactory;
import name.jchein.ptflibs.identity.ulid.ULIDFactory;

public class WiringFixture {
	private final SimpleULIDFactory simpleStatelessFactory;
	private final MonotonicULIDFactory monotonicFactory;

	public WiringFixture(
		final Clock clock,
		final ULIDRandomBitsStrategy random
//		final Relocatable relocatable
	) {
		this.simpleStatelessFactory =
			new SimpleULIDFactory(clock, random);
		this.monotonicFactory =
			new MonotonicULIDFactory(clock, random);
	}
/*		final long RESERVED_ID = 0x9dcc3bea5560L;
		
//		relocatable.updateLocation(locationId, locationBits); */
	
	public ULIDFactory getFactory() {
		return this.monotonicFactory;
	}
	
	public ULIDFactory getSimpleFactory() {
		return this.simpleStatelessFactory;
	}
	
	public ULIDFactory getMonotonicFactory() {
		return this.monotonicFactory;
	}
}
