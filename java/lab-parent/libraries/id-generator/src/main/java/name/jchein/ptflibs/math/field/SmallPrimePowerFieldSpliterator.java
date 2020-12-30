package name.jchein.ptflibs.math.field;

import java.util.Spliterator;
import java.util.function.LongConsumer;

class SmallPrimePowerFieldSpliterator implements ForkResetSpliteratorOfLong {
	final long g;
    final long pn;
    long t;
    long resetTo;
    long onResetRemaining;
    long currentRemaining;

	SmallPrimePowerFieldSpliterator(final long pn, final long p, final long g) {
		this.g = g;
		this.pn = pn;
		this.t = g;
		this.resetTo = g;
		this.onResetRemaining = pn - p - 2;
		this.currentRemaining = this.onResetRemaining;
	}
	
	/**
	 * 
	 * @param origin
	 * @param isReset If true, the new copy is to represent a reset of origin. rewinding
	 *                back to its most recent fork.  If false, the new copy is a fork, 
	 *                returning the same sequence as the original, but rewinding to its
	 *                present state instead of the previous fork.
	 */
	private SmallPrimePowerFieldSpliterator(
		final SmallPrimePowerFieldSpliterator origin, final boolean isReset
	) {
		this.g = origin.g;
		this.pn = origin.pn;
		this.t = isReset ? origin.resetTo : origin.t;
		this.resetTo = isReset ? origin.resetTo : origin.t;
		this.onResetRemaining =
			isReset ? origin.onResetRemaining : origin.currentRemaining;
		this.currentRemaining =
			isReset ? origin.onResetRemaining : origin.currentRemaining;
	}

    @Override
    public Spliterator.OfLong trySplit() {
    	return null;
    }

    @Override
    public boolean tryAdvance(LongConsumer consumer) {
    	if (this.t == 1) {
    		return false;
    	}

        final long v = this.t;
        this.t = (v * this.g) % this.pn; 
        this.currentRemaining -= 1;
        consumer.accept(v);
        return true;
    }
    
    @Override
    public long estimateSize() {
    	return this.currentRemaining;
    }
    
    @Override
    public int characteristics() {
    	return Spliterator.ORDERED |
    		Spliterator.DISTINCT |
    		Spliterator.IMMUTABLE |
    		Spliterator.NONNULL |
    		Spliterator.SIZED;
    }
    
    @Override
    public SmallPrimePowerFieldSpliterator fork() {
    	return new SmallPrimePowerFieldSpliterator(this, false);
    }
    
    @Override
    public SmallPrimePowerFieldSpliterator reset() {
    	return new SmallPrimePowerFieldSpliterator(this, true);
    }
}
