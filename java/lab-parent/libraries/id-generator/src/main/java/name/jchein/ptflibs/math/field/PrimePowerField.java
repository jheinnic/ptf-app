package name.jchein.ptflibs.math.field;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

public final class PrimePowerField {
	final int n;
	final BigInteger p;
	final BigInteger pn;
	
	private PrimePowerField(final BigInteger p, final int n) {
		this.p = p;
		this.n = n;
	    this.pn = p.pow(n);
	}
	
	public static PrimePowerField getField(BigInteger p, int power) {
		return new PrimePowerField(p, power);
	}

	public static PrimePowerField generateField(
		final int maxElementBits, final int minPrimeBits, final int maxPrimeBits
	) {
		if (minPrimeBits > maxPrimeBits) {
			throw new IllegalArgumentException("minPrimeBits cannot exceed maxPrimeBits");
		}
		if (minPrimeBits > maxElementBits) {
			throw new IllegalArgumentException("minPrimeBits cannot exceed maxElementBits");
		}
		if (maxPrimeBits >= maxElementBits) {
			throw new IllegalArgumentException("maxPrimeBits must be smaller than maxElementBits");
		}
		int bestExp = -1;
		BigInteger target = BigInteger.ONE.shiftLeft(maxElementBits).subtract(BigInteger.ONE);
		BigInteger bestDist = target;
		BigInteger bestPrime = BigInteger.ONE;
		int nextPrimeBits = minPrimeBits;
		Random random = new SecureRandom();
		while (nextPrimeBits <= maxPrimeBits) {
			final int baseExp = maxElementBits / nextPrimeBits;
			for (int ii=0; ii<6; ii++) {
				final BigInteger nextPrime =
					BigInteger.probablePrime(nextPrimeBits, random);
				int nextExp = baseExp;
				BigInteger nextPower = nextPrime.pow(baseExp);
				BigInteger nextDist = target.subtract(nextPower);
				BigInteger candidatePower = nextPower.multiply(nextPrime);
				BigInteger candidateDist = target.subtract(candidatePower);
				while(candidateDist.signum() > -1) {
					nextPower = candidatePower;
					nextDist = candidateDist;
					nextExp += 1;
					candidatePower = candidatePower.multiply(nextPrime);
					candidateDist = target.subtract(candidatePower);
				}
				if (bestDist.subtract(nextDist).signum() > -1) {
					bestPrime = nextPrime;
					bestDist = nextDist;
					bestExp = nextExp;
				}
			}
			nextPrimeBits += 1;
		}
		PrimePowerField retVal = new PrimePowerField(bestPrime, bestExp);
		System.out.println(
			String.format(
				"Selected prime <%s> with <%d> bits raised to <%d> for <%d> of <%d> target bits, within <%s>",
				bestPrime, bestPrime.bitLength(),
				bestExp, retVal.pn.bitLength(),
				maxElementBits, bestDist
			)
		);
		return retVal;
	}

	public ForkResetSpliteratorOfLong findBasePrimeGenerator() {
		BigInteger g = this.p.shiftRight(1);
		while((g.signum() == 1) && ! this.verifyBasePrimeGenerator(g)) {
			g = g.subtract(BigInteger.ONE);
		}
		if (g.signum() != 1) {
			g = this.p.shiftRight(1).add(BigInteger.ONE);
			while(! this.p.equals(g) && ! this.verifyBasePrimeGenerator(g)) {
				g = g.add(BigInteger.ONE);
			}
		}
		if (this.p.equals(g)) {
			throw new IllegalStateException(
				String.format("No generator could be found for <%s>", this.p.toString())
			);

		}

		return streamForGenerator(g);
	}
	
	public ForkResetSpliteratorOfLong getPowerPrimeSequence(final BigInteger g) {
//		if (this.verifyBasePrimeGenerator(g)) {
		return streamForGenerator(g);
//		}
//		throw new IllegalArgumentException(
//			String.format("<%s> is not a generator for <%s>", g, this.p));
	}

	boolean verifyBasePrimeGenerator(final BigInteger g) {
		Objects.requireNonNull(g, "Generator must be non-null and positive");
		if (g.signum() != 1) {
			throw new IllegalArgumentException("g must be a positive value");
		}
		if (p.subtract(g).signum() != 1) {
			throw new IllegalArgumentException("g must be smaller than p");
		}
		BigInteger counter = BigInteger.valueOf(2);
		BigInteger latest = g;
		while(! BigInteger.ONE.equals(latest) && ! this.p.equals(counter)) {
			latest = g.modPow(counter, p);
			counter = counter.add(BigInteger.ONE);
		}
//		System.out.println(
//			String.format(
//				"Stopped with p = <%s>, g = <%s>, counter = <%s>, latest = <%s>",
//				this.p, g, counter, latest
//			)
//		);
		return this.p.equals(counter);
	}

	/**
	 * Returns a stream that contains all (p^n - p - 2) integers that are 
	 * greater than 1, less than p^n, and not divisible by p.
	 * 
	 * Requires a prime, an nonent, and a generator for the multiplicative
	 * @param g
	 * @param p
	 * @param n
	 * @return
	 */
	ForkResetSpliteratorOfLong streamForGenerator(BigInteger g) {
		final int pnBits = this.pn.bitLength();
		if (pnBits > 63) {
			throw new IllegalArgumentException(
				"Power of prime cannot exceed 64 bits in length for a LongStream"
			);
		}
		final int multBits = this.pn.subtract(BigInteger.ONE).multiply(g).bitLength();

        final ForkResetSpliteratorOfLong iterator;
		if (multBits <= 63) {
	        iterator = new SmallPrimePowerFieldSpliterator(
	        	this.pn.longValueExact(),
	        	this.p.longValueExact(), 
	        	g.longValueExact());
		} else {
	        iterator = new LargePrimePowerFieldSpliterator(this.pn, this.p, g);
		}

	    return iterator;
	}
}