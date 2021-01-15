package name.jchein.ptflibs.math.field;

import java.math.BigInteger;
import java.util.Spliterator;
import java.util.function.LongConsumer;

/**
 * Variation on the ForkResetSpliteratorOfLong that can handle cases where the previous
 * element multiplied by the generator can potentially overflow the storage size of a 
 * long before performing a modulo operation, but the prime power used for the group's
 * modulo limits the largest group member's storage size to 63 bits or less.
 * Because every group member requires 63 bits or less, it is possible to use a 
 * LongSpliterator to access the entire sequence.
 * 
 * Although the return values are no more than 63 bits in size, this class
 * can support prime power groups where the product of previous element and
 * generator can overflow 63 bits by virtue of using BigInteger to store generator state
 * and to perform intermediate computation.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
 * 
 * @author jheinnic
 */
class BigLongGroupGeneratorSequence implements LongGroupGeneratorSpliterator {
	final BigInteger g;
	final BigInteger p;
    final BigInteger pn;
    final BigInteger groupSize;
    BigInteger t;
    BigInteger remaining;
    BigInteger tOnReset;
    BigInteger remainingOnReset;

	BigLongGroupGeneratorSequence(
		final BigInteger pn, final BigInteger p,
		final BigInteger g, final BigInteger exp
	) {
		this.g = g;
		this.p = p;
		this.pn = pn;
		this.groupSize = pn.subtract(
			pn.divide(p)
		).subtract(BigInteger.ONE);
		this.t = g.modPow(exp, pn);
		this.remaining = this.groupSize.subtract(exp);
		this.tOnReset = this.t;
		this.remainingOnReset = this.remaining;
	}
	
	private BigLongGroupGeneratorSequence(
		final BigLongGroupGeneratorSequence origin, final boolean isReset
	) {
		this.g = origin.g;
		this.p = origin.p;
		this.pn = origin.pn;
		this.groupSize = origin.groupSize;
		this.t = isReset ? origin.tOnReset : origin.t;
		this.remaining = isReset ? origin.remainingOnReset : origin.remaining;
		this.tOnReset = this.t;
		this.remainingOnReset = this.remaining;
	}
	
	@Override
	public long getP() {
		return this.p.longValueExact();
	}

	@Override
	public long getPN() {
		return this.pn.longValueExact();
	}

	@Override
	public long getG() {
		return this.g.longValueExact();
	}

	@Override
	public long getT() {
		return this.t.longValueExact();
	}

    @Override
    public long getRemaining() {
    	return this.remaining.longValueExact();
    }
    
    @Override
    public long getExponent() {
    	return this.groupSize.subtract(this.remaining).longValueExact();
    }

    @Override
    public Spliterator.OfLong trySplit() {
    	return null;
    }

    @Override
    public boolean tryAdvance(LongConsumer consumer) {
    	if (BigInteger.ONE.equals(this.t)) {
    		return false;
    	}

    	final long v = this.t.longValueExact();
        this.t = this.t.multiply(this.g).mod(this.pn); 
        this.remaining = this.remaining.subtract(BigInteger.ONE);
        consumer.accept(v);
        return true;
    }
    
    @Override
    public long estimateSize() {
    	return this.remaining.longValueExact();
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
    public BigLongGroupGeneratorSequence fork() {
    	return new BigLongGroupGeneratorSequence(this, false);
    }
    
    @Override
    public BigLongGroupGeneratorSequence reset() {
    	return new BigLongGroupGeneratorSequence(this, true);
    }
}
