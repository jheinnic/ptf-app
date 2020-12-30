package name.jchein.ptflibs.math.field;

import java.math.BigInteger;
import java.util.Spliterator;
import java.util.function.Consumer;

class BigIntegerPrimePowerFieldSpliterator implements ForkResetSpliteratorOfBigInteger {
	final BigInteger g;
    final BigInteger pn;
    BigInteger nextPower;
    final BigInteger resetPower;
    final BigInteger finalPower;

    BigIntegerPrimePowerFieldSpliterator(final BigInteger pn, final BigInteger p, final BigInteger g) {
		this.g = g;
		this.pn = pn;
		this.nextPower = BigInteger.ONE.add(BigInteger.ONE);
		this.resetPower = this.nextPower;
		this.finalPower = pn.subtract(p).subtract(this.nextPower);
	}
	
	private BigIntegerPrimePowerFieldSpliterator(
		final BigIntegerPrimePowerFieldSpliterator origin, final boolean isReset
	) {
		this.g = origin.g;
		this.pn = origin.pn;
		this.nextPower = isReset ? origin.resetPower : origin.nextPower;
		this.resetPower = isReset ? origin.resetPower : origin.nextPower;
		this.finalPower = origin.finalPower;
	}

    @Override
    public BigIntegerPrimePowerFieldSpliterator trySplit() {
    	return null;
    }

    @Override
    public boolean tryAdvance(Consumer<? super BigInteger> consumer) {
    	if (this.finalPower.equals(this.nextPower)) {
    		return false;
    	}

    	final BigInteger v = this.g.modPow(this.nextPower, this.pn);
        this.nextPower = this.nextPower.add(BigInteger.ONE);
        consumer.accept(v);
        return true;
    }
    
    @Override
    public long estimateSize() {
    	BigInteger numRemaining = this.finalPower.subtract(this.nextPower);
    	if (numRemaining.bitLength() < 64) {
    		return numRemaining.longValueExact();
    	}
    	return Long.MAX_VALUE;
    }
    
    @Override
    public long getExactSizeIfKnown() {
    	BigInteger numRemaining = this.finalPower.subtract(this.nextPower);
    	if (numRemaining.bitLength() < 64) {
    		return numRemaining.longValueExact();
    	}
    	return -1;
    }
    
    @Override
    public int characteristics() {
    	return Spliterator.ORDERED |
    		Spliterator.DISTINCT |
    		Spliterator.IMMUTABLE |
    		Spliterator.NONNULL;
    }
    
    @Override
    public BigIntegerPrimePowerFieldSpliterator fork() {
    	return new BigIntegerPrimePowerFieldSpliterator(this, false);
    }
    
    @Override
    public BigIntegerPrimePowerFieldSpliterator reset() {
    	return new BigIntegerPrimePowerFieldSpliterator(this, true);
    }}
