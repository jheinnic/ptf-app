package name.jchein.ptflibs.identity.ulid;

/**
 * Interface used by a single threaded ULIDFactory to drive generation of its 80-bit random field.  
 * 
 * To concurrently generate two or more ULID series in parallel, it is necessary to use two or more ULIDFactories,
 * each of which requires a dedicated thread in order to use instances of this interface.
 * 
 * Each call to onBackTick(), onClockTick(), or onNext() is expected to advance internal state forward to a new
 * answer.  The remaining getXXX() methods provide access to different bit ranges of this same single answer until
 * their state is altered by the next call to an onXXX() method.  For multiple ULID generators to collaborate on a
 * single such artifact, there would need to be an external critical section/synchronization mutex spanning the first
 * call to an onXXX() method to the last call to a getXXX() method.
 * 
 * @author jheinnic
 *
 */
public interface ULIDRandomBitsStrategy {
	/**
	 * Signals "next" and also reversal of clock sequence, indicating that randomizer must advance to a next
	 * value, but not one it has already returned.  Furthermore, it must also no longer rewind to a previously
	 * returned value whenever onClockTick() is called, although rewinding to the immediate next value or any
	 * that follow it is still valid.
	 * 
	 * Stateful generators will want to use this as a cue to setup a new sequence and ensure that
	 * onClockTick() cannot rewind to a value from the previous sequence.  Stateless random generators
	 * will likely be unable to leverage this signal because they have no concept of disjoint sequences.
	 * 
	 * Deterministic pseudorandom generators will minimally want to change their seed to switch to a new
	 * sequence, but may want to augment randomness with some bits set aside for an "epoch" field.  Future
	 *
	 */
	public void onBackTick4040(long timetstamp, EqualSplitBitsProvider callback);

	public void onBackTickIntLong(long timestamp, LilHiBigLoBitsProvider callback);
	
	/**
	 * Signals "next" and the beginning of a new clock tick.  Generators that either use state to produce a
	 * deterministic sequence of values or to remember a past history of values may use this signal to rewind
	 * to repeat previously used values, since those values are certain to be paired with different clock
	 * values now.
	 */
	public void onForwardTick4040(long timestamp, EqualSplitBitsProvider callback);

	public void onForwardTickIntLong(long timestamp, LilHiBigLoBitsProvider callback);
	
	/**
	 * Signals next, but with the clock value unchanged.  Generators should advance to return their next
	 * value, or if using random values without a concept of sequence, a different value at any rate.
	 */
	public void onSameTick4040(long timestamp, EqualSplitBitsProvider callback);

	public void onSameTickIntLong(long timestamp, LilHiBigLoBitsProvider callback);
}
