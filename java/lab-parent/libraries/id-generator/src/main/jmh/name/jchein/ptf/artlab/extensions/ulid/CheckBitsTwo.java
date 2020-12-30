package name.jchein.ptf.artlab.extensions.ulid;

import java.time.Clock;
import java.time.Duration;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import name.jchein.ptflibs.identity.ulid.AbstractULIDRandomBitsStrategy;
//import name.jchein.ptflibs.identity.ulid.RandomULIDRandomBitsStrategy;
import name.jchein.ptflibs.identity.ulid.ULID;
import name.jchein.ptflibs.identity.ulid.ULIDFactory;
import name.jchein.ptflibs.math.field.ForkResetSpliteratorOfLong;
import name.jchein.ptflibs.math.field.PrimePowerField;

@State(Scope.Thread)
public class CheckBitsTwo {

	public static void main(String[] args) {
		final long RESERVED_ID = 0x20c3bea560L;
		final Clock systemClock = Clock.systemUTC();
		final Clock tickClock = Clock.tick(systemClock, Duration.ofMillis(2500));
		final AbstractULIDRandomBitsStrategy randomness = new AbstractULIDRandomBitsStrategy(
			RESERVED_ID, 50, (byte) 2, 43, 21, 13);
		final WiringFixture wiring = new WiringFixture(tickClock, randomness); // , RESERVED_ID, 43);
		final ULIDFactory monotonicFactory = wiring.getFactory();
		
		final PrimePowerField bigPpf = PrimePowerField.generateField(48, 17, 50);
		final ForkResetSpliteratorOfLong series = bigPpf.findBasePrimeGenerator();


		for (int jj = 0; jj < 100; jj++) {
			ULID ulidSeven = monotonicFactory.nextULID();
			for (int ii = 0; ii < 10000; ii++) {
				ulidSeven = monotonicFactory.nextULID();
			}

			ULID ulidOne = monotonicFactory.nextULID();
			ULID ulidTwo = monotonicFactory.nextULID();
			ULID ulidThree = monotonicFactory.nextULID();
			ULID ulidFour = monotonicFactory.nextULID();
			ULID ulidFive = monotonicFactory.nextULID();
			ULID ulidSix = monotonicFactory.nextULID();

			System.out.println(String.format("<%s> <%s> <%s> <%s> <%s> <%s> <%s>", ulidOne, ulidTwo, ulidThree, ulidFour,
					ulidFive, ulidSix, ulidSeven));
			
			long[] sequence = new long[12];
			for (int ii = 0; ii < 12; ii++) {
				final int idx = ii;
				series.tryAdvance((long nextLong) -> {
					sequence[idx] = nextLong;
				});
			}
			System.out.println(
				String.format("<%s>", sequence));
		}
	}
}
