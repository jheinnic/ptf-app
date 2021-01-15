package name.jchein.ptflibs.math.field;

import java.math.BigInteger;
import java.util.Spliterator;
import java.util.function.LongConsumer;

class PrimitiveGroupGeneratorSequence implements LongGroupGeneratorSpliterator {
	final long g;
	final long p;
    final long pn;
    long t;
    long tOnReset;
    long remaining;
    long remainingOnReset;
    long groupSize;

	PrimitiveGroupGeneratorSequence(
		final long pn, final long p, final long g, final long exp
	) {
		this.g = g;
		this.p = p;
		this.pn = pn;
		this.groupSize = pn - (pn / p) - 1;
		this.t = BigInteger.valueOf(g)
			.modPow(
				BigInteger.valueOf(exp),
				BigInteger.valueOf(pn)
			).longValueExact();
		this.remaining = this.groupSize - exp;
		this.tOnReset = this.t;
		this.remainingOnReset = this.remaining;
	}
	
	/**
	 * 
	 * @param origin
	 * @param isReset If true, the new copy is to represent a reset of origin. rewinding
	 *                back to its most recent fork.  If false, the new copy is a fork, 
	 *                returning the same sequence as the original, but rewinding to its
	 *                present state instead of the previous fork.
	 */
	private PrimitiveGroupGeneratorSequence(
		final PrimitiveGroupGeneratorSequence origin, final boolean isReset
	) {
		this.g = origin.g;
		this.p = origin.p;
		this.pn = origin.pn;
		this.groupSize = origin.groupSize;
		this.t = isReset ? origin.tOnReset : origin.t;
		this.remaining =
			isReset ? origin.remainingOnReset : origin.remaining;
		this.tOnReset = this.t;
		this.remainingOnReset = this.remaining;
	}
	
	@Override
	public long getP() {
		return this.p;
	}

	@Override
	public long getPN() {
		return this.pn;
	}

	@Override
	public long getG() {
		return this.g;
	}

	@Override
	public long getT() {
		return this.t;
	}

    @Override
    public long getRemaining() {
    	return this.remaining;
    }
    
    @Override
    public long getExponent() {
    	return this.groupSize - this.remaining;
    }

    @Override
    public Spliterator.OfLong trySplit() {
    	return null;
    }

    @Override
    public boolean tryAdvance(LongConsumer consumer) {
    	if (this.t == 1) {
    		// TODO: Assert that exponent == groupSize?
    		return false;
    	}

        final long v = this.t;
        this.t = (this.t * this.g) % this.pn; 
        this.remaining -= 1;
        consumer.accept(v);
        return true;
    }
    
    @Override
    public long estimateSize() {
    	return this.remaining;
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
    public PrimitiveGroupGeneratorSequence fork() {
    	return new PrimitiveGroupGeneratorSequence(this, false);
    }
    
    @Override
    public PrimitiveGroupGeneratorSequence reset() {
    	return new PrimitiveGroupGeneratorSequence(this, true);
    }
}
