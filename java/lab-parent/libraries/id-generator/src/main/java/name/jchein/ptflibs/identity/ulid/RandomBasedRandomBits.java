package name.jchein.ptflibs.identity.ulid;

import java.util.Random;

import javax.validation.constraints.NotNull;

public final class RandomBasedRandomBits implements ULIDRandomBitsStrategy {
	private final Random random;
	
	public RandomBasedRandomBits(@NotNull Random random) {
		this.random = random;
	}

	@Override
	public long getRandomHi40() {
		return this.random.nextLong() & Constants.RANDOM_40_MASK;
	}
	
	@Override
	public long getRandomLo40() {
		return this.random.nextLong() & Constants.RANDOM_40_MASK;
	}
	
	@Override
	public int getRandomHi16() {
		return this.random.nextInt((int) Constants.RANDOM_16_MASK);
	}
	
	@Override
	public long getRandomLo64() {
		return this.random.nextLong();
	}
}
