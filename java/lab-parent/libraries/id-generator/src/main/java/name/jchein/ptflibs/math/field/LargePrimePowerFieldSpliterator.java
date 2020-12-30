package name.jchein.ptflibs.math.field;

import java.math.BigInteger;
import java.util.Spliterator;
import java.util.function.LongConsumer;

/**
 * Variation on the ForkResetSpliteratorOfLong that can handle cases where the previous
 * element multiplied by the generator can potentially overflow the storage size of a 
 * long before performing a modulo operation, but the prime power used for the modulo
 * limit itself fits within a primitive long value.
 * 
 * @author jheinnic
 */
class LargePrimePowerFieldSpliterator implements ForkResetSpliteratorOfLong {
	final BigInteger g;
    final BigInteger pn;
    BigInteger t;
    BigInteger resetTo;
    long origRemaining;
    long currentRemaining;

	LargePrimePowerFieldSpliterator(final BigInteger pn, final BigInteger p, final BigInteger g) {
		this.g = g;
		this.pn = pn;
		this.t = g;
		this.resetTo = g;
		this.origRemaining = pn.longValue() - p.longValue() - 2;
		this.currentRemaining = this.origRemaining;
	}
	
	private LargePrimePowerFieldSpliterator(
		final LargePrimePowerFieldSpliterator origin, final boolean isReset
	) {
		this.g = origin.g;
		this.pn = origin.pn;
		this.t = isReset ? origin.resetTo : origin.t;
		this.resetTo = isReset ? origin.resetTo : origin.t;
		this.origRemaining = isReset ? origin.origRemaining : origin.currentRemaining;
		this.currentRemaining = isReset ? origin.origRemaining : origin.currentRemaining;
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
    public LargePrimePowerFieldSpliterator fork() {
    	return new LargePrimePowerFieldSpliterator(this, false);
    }
    
    @Override
    public LargePrimePowerFieldSpliterator reset() {
    	return new LargePrimePowerFieldSpliterator(this, true);
    }
}
