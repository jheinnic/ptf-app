package name.jchein.ptflibs.identity.ulid;

public class FixedUDIDRandomBitsStrategy
implements ULIDRandomBitsStrategy {
	private int randHi16;
	private long randHi40;
	private long randLo40;
	private long randLo64;
	
	public FixedUDIDRandomBitsStrategy(int randHi16, long randHi40, long randLo40, long randLo64) {
		this.randHi16 = randHi16;
		this.randHi40 = randHi40;
		this.randLo40 = randLo40;
		this.randLo64 = randLo64;
	}
	
	public void onBackTick4040(Random4040Callback callback) {
//		this.onBackTick();
		callback.accept(this.randHi40, this.randLo40);
	}

	public void onBackTickIntLong(RandomIntLongCallback callback) {
//		this.onBackTick();
		callback.accept(this.randHi16, this.randLo64);
	}
	
	/**
	 * Signals "next" and the beginning of a new clock tick.  Generators that either use state to produce a
	 * deterministic sequence of values or to remember a past history of values may use this signal to rewind
	 * to repeat previously used values, since those values are certain to be paired with different clock
	 * values now.
	 */
	public void onForwardTick4040(Random4040Callback callback) {
//		this.onClockTick();
		callback.accept(this.randHi40, this.randLo40);
	}

	public void onForwardTickIntLong(RandomIntLongCallback callback) {
//		this.onClockTick();
		callback.accept(this.randHi16, this.randLo64);
	}
	
	/**
	 * Signals next, but with the clock value unchanged.  Generators should advance to return their next
	 * value, or if using random values without a concept of sequence, a different value at any rate.
	 */
	public void onSameTick4040(Random4040Callback callback) {
//		this.onNext();
		callback.accept(this.randHi40, this.randLo40);
	}

	public void onSameTickIntLong(RandomIntLongCallback callback) {
//		this.onNext();
		callback.accept(this.randHi16, this.randLo64);;
	}
	
//	protected void onBackTick() {
//		this.onNext();
//		this.resetHi16 = this.randHi16;
//		this.resetHi40 = this.randHi40;
//		this.resetLo40 = this.randLo40;
//		this.resetLo64 = this.randLo64;
//	}
//	
//	protected void onClockTick() {
//		this.randHi16 = this.resetHi16;
//		this.randHi40 = this.resetHi40;
//		this.randLo40 = this.resetLo40;
//		this.randLo64 = this.resetLo64;
//	}
//	
//	protected void onNext() {
//		if (this.randLo40 == Constants.BASE_RANDOM_LO_40_MASK) {
//			if (this.randHi40 == Constants.BASE_RANDOM_LO_40_MASK) {
//				this.randLo40 = 0;
//				this.randHi40 = 0;
//				this.randLo64 = 0;
//				this.randHi16 = 0;
//				return;
//			}
//			this.randLo40 = 0;
//			this.randHi40 = this.randHi40 + 1;
//			
//			if (this.randLo64 == Constants.BASE_RANDOM_LO_64_MASK) {
//				this.randLo64 = 0;
//				this.randHi16 = this.randHi16 + 1;
//			} else {
//				this.randLo64 = this.randLo64 + 1;
//			}
//		} else {
//			this.randLo40 = this.randLo40 + 1;
//			this.randLo64 = this.randLo64 + 1;
//		}
//	}
}