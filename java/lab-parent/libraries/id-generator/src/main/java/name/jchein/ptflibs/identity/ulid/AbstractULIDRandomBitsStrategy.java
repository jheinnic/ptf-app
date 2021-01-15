package name.jchein.ptflibs.identity.ulid;

import java.math.BigInteger;

import org.hibernate.validator.constraints.Range;

import name.jchein.ptflibs.math.field.LongGroupGeneratorSpliterator;
import name.jchein.ptflibs.math.field.PrimePowerGroup;

public class AbstractULIDRandomBitsStrategy implements ULIDRandomBitsStrategy {
	private final int epochShift;
	private final long epochMask;
	private final long seriesMask;
	private final long seriesFlowLimit;

	private final PrimePowerGroup epochSource;
	private final BigInteger constantBits;

	private LongGroupGeneratorSpliterator activeEpochIterator;
	private BigInteger activeRandom;
	private BigInteger activeEpoch;
	private long activeSeries;

	private BigInteger resetRandom;
	private BigInteger resetEpoch;
	private long resetSeries;
	
	protected int randHi16;
	protected long randHi40;
	protected long randLo40;
	protected long randLo64;

	/* int randHi16, long randHi40, long randLo40, long randLo64) { */
	public AbstractULIDRandomBitsStrategy(
		@Range(min = 0) final long node,
		@Range(min = 0) final int initialSeries,
		@Range(min = 0, max = 7) final byte variant,
		@Range(min = 0, max = 63) final int nodeBits,
		@Range(min = 0, max = 63) final int epochBits,
		@Range(min = 2, max = 63) final int seriesBits
	) {
		final int totalBits =
			nodeBits + Constants.VARIANT_BITS_LEN + epochBits + seriesBits;
		if (totalBits != 80) {
			throw new IllegalArgumentException(
				"Bit counts add to " + totalBits + ", not 80");
		}
		
		// Pack the immutable bits--node and variant.
		int nodeShift = 80 - nodeBits;
		long nodeMask = (0x1L << nodeBits) - 1;
		this.constantBits = BigInteger.valueOf(node & nodeMask)
			.shiftLeft(nodeShift)
			.or(BigInteger.valueOf(variant));

		this.seriesMask = (0x1L << seriesBits) - 1;
		this.epochMask = (0x1L << epochBits) - 1;
		this.epochShift = nodeShift - epochBits;
		this.seriesFlowLimit = (initialSeries == 0) ? this.seriesMask : (initialSeries - 1);
		this.epochSource = PrimePowerGroup.findGroupByCharacteristic(
			epochBits,
			Constants.MIN_EPOCH_FIELD_PRIME_BITS,
			Constants.MAX_EPOCH_FIELD_PRIME_BITS
		);
		this.activeEpochIterator =
			this.epochSource.findBasePrimeGenerator();
		
		this.advanceNextEpoch();
		this.setResetTarget();
	}

	public void onBackTick4040(Random4040Callback callback) {
		this.onBackTick();
		callback.accept(this.randHi40, this.randLo40);
	}

	public void onBackTickIntLong(RandomIntLongCallback callback) {
		this.onBackTick();
		callback.accept(this.randHi16, this.randLo64);
	}

	/**
	 * Signals "next" and the beginning of a new series tick. Generators that either
	 * use state to produce a deterministic sequence of values or to remember a past
	 * history of values may use this signal to rewind to repeat previously used
	 * values, since those values are certain to be paired with different series
	 * values now.
	 */
	public void onForwardTick4040(Random4040Callback callback) {
		this.onSeriesTick();
		callback.accept(this.randHi40, this.randLo40);
	}

	public void onForwardTickIntLong(RandomIntLongCallback callback) {
		this.onSeriesTick();
		callback.accept(this.randHi16, this.randLo64);
	}

	/**
	 * Signals next, but with the series value unchanged. Generators should advance
	 * to return their next value, or if using random values without a concept of
	 * sequence, a different value at any rate.
	 */
	public void onSameTick4040(Random4040Callback callback) {
		this.onNext();
		callback.accept(this.randHi40, this.randLo40);
	}

	public void onSameTickIntLong(RandomIntLongCallback callback) {
		this.onNext();
		callback.accept(this.randHi16, this.randLo64);
	}

	protected void onBackTick() {
		this.advanceNextEpoch();
		this.setResetTarget();
		this.unpackActiveReturn();
	}
	
	protected void onSeriesTick() {
		this.resetActiveRandom();
		this.unpackActiveReturn();
	}
	
	protected void onNext() {
		// TODO: This is too general--it should be possible to implement more
		//       efficient unpacking that only unpacks the low order bits with
		//       series updates (series's maximum bit length guarantees it fits
		//       within both lsb40 and lsb64).  It should also detect if and
		//       when updating the epoch field affects bits of msb16 and msb40,
		//       just msb40, or neither msb16 nor msb40.
		this.advanceNextSeries();
		this.unpackActiveReturn();
	}
	
	private void resetActiveRandom() {
		this.activeRandom = resetRandom;
		this.activeSeries = resetSeries;
		this.activeEpoch = resetEpoch;
		this.activeEpochIterator = this.activeEpochIterator.reset();
	}

	private void setResetTarget() {
		this.resetRandom = this.activeRandom;
		this.resetSeries = this.activeSeries;
		this.resetEpoch = this.activeEpoch;
		this.activeEpochIterator = this.activeEpochIterator.fork();
	}
		
	private void advanceNextEpoch() {
		if (! this.activeEpochIterator.tryAdvance(
			(long nextEpoch) -> {
				long epochBits = nextEpoch & this.epochMask;
				if (epochBits != nextEpoch) {
					throw new IllegalStateException(
						String.format(
							"Epoch generator overflowed its allocation region: <%d> != <%d> & <%d>",
							epochBits, nextEpoch, this.epochMask
						)
					);
				}
				this.activeSeries = (this.seriesFlowLimit == this.seriesMask)
					? 0 : (this.seriesFlowLimit + 1);
				this.activeEpoch = this.constantBits.or(
					BigInteger.valueOf(epochBits)
						.shiftLeft(this.epochShift)
					);
				this.activeRandom = this.activeEpoch.or(
					BigInteger.valueOf(this.activeSeries)
						.shiftLeft(Constants.SERIES_BITS_SHIFT)
				);
			}
		)) {
			throw new IllegalStateException("Epoch sequence iterator ran out options!");
		}
	}
	
	private void advanceNextSeries() {
		if (this.activeSeries == this.seriesFlowLimit) {
			this.advanceNextEpoch();
		} else {
			this.activeSeries = (this.activeSeries == this.seriesMask)
				? 0 : (this.activeSeries + 1);
			this.activeRandom = this.activeEpoch.or(
				BigInteger.valueOf(this.activeSeries)
					.shiftLeft(Constants.SERIES_BITS_SHIFT)
			);
		}
	}
	
	private void unpackActiveReturn() {
		this.randHi16 = this.activeRandom.shiftRight(64).intValueExact();
		this.randHi40 = this.activeRandom.shiftRight(40).longValueExact();
		this.randLo40 = this.activeRandom.and(Constants.LO_40_MASK).longValue();
		this.randLo64 = this.activeRandom.and(Constants.LO_64_MASK).longValue();
	}
}