package name.jchein.ptflibs.math.field;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

public final class PrimePowerGroup {
	final int exp;
	final BigInteger p;
	final BigInteger pn;
	
	private PrimePowerGroup(final BigInteger p, final int exp) {
		this.p = p;
		this.exp = exp;
	    this.pn = p.pow(exp);
	}
	
	public static PrimePowerGroup getGroupByPrimePower(BigInteger p, int exp) {
		return new PrimePowerGroup(p, exp);
	}

	public static PrimePowerGroup findGroupByCharacteristic(
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
				BigInteger primeMinusOne = nextPrime.subtract(BigInteger.ONE); 
				BigInteger nextPower = BigInteger.ZERO;
				BigInteger nextDist = target;
				BigInteger candidatePower = nextPrime.pow(nextExp);
				BigInteger candidateAdjusted = candidatePower.divide(nextPrime).multiply(primeMinusOne);
				BigInteger candidateDist = target.subtract(candidateAdjusted);
				while(candidateDist.signum() > -1) {
					nextPower = candidatePower;
					nextDist = candidateDist;
					nextExp += 1;
					candidatePower = nextPower.multiply(nextPrime);
				    candidateAdjusted = candidatePower.divide(nextPrime).multiply(primeMinusOne);
					candidateDist = target.subtract(candidateAdjusted);
				}
				if (bestDist.subtract(nextDist).signum() > -1) {
					bestPrime = nextPrime;
					bestDist = nextDist;
					bestExp = nextExp - 1;
				}
			}
			nextPrimeBits += 1;
		}
		PrimePowerGroup retVal =
			new PrimePowerGroup(bestPrime, bestExp);
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

	public boolean verifyBasePrimeGenerator(final BigInteger g) {
		Objects.requireNonNull(g, "Generator must be non-null and positive");
		if (g.signum() != 1) {
			throw new IllegalArgumentException("g must be a positive value");
		}
		if (p.subtract(g).signum() != 1) {
			throw new IllegalArgumentException("g must be smaller than p");
		}
		if (
			! BigInteger.ONE.equals(
				g.modPow(
					this.p.subtract(BigInteger.ONE), this.p))
		) {
			return false;
		}
		BigInteger counter = BigInteger.valueOf(2);
		BigInteger latest = g;
		while(! (latest.equals(BigInteger.ONE) || counter.equals(this.p))) {
			// TODO: Verify the assumption that iterative multiplication is more efficient here
			//       because we have to check for a cycle (return to 1) on each iteration anyhow.
			// latest = g.modPow(counter, p);
			latest = g.multiply(latest).mod(p);
			counter = counter.add(BigInteger.ONE);
		}
		return this.p.equals(counter);
	}

//	public LongGroupGeneratorSpliterator findBasePrimeGenerator() {
//		BigInteger g = this.p.shiftRight(1);
//		while((g.signum() == 1) && ! this.verifyBasePrimeGenerator(g)) {
//			g = g.subtract(BigInteger.ONE);
//		}
//		if (g.signum() != 1) {
//			g = this.p.shiftRight(1).add(BigInteger.ONE);
//			while(! this.p.equals(g) && ! this.verifyBasePrimeGenerator(g)) {
//				g = g.add(BigInteger.ONE);
//			}
//		}
//		if (this.p.equals(g)) {
//			throw new IllegalStateException(
//				String.format("No generator could be found for <%s>", this.p.toString())
//			);
//
//		}
//
//		return this.getPowerPrimeSequence(g, BigInteger.ONE, true);
//	}


	public LongGroupGeneratorSpliterator findBasePrimeGenerator() {
		BigInteger gLo = this.p.shiftRight(1);
		BigInteger gHi = gLo.add(BigInteger.ONE);
		boolean hiGen  = this.verifyBasePrimeGenerator(gHi);
		boolean noGen  = ! hiGen;
		if (noGen && gLo.shiftLeft(1).equals(this.p)) {
			noGen = ! this.verifyBasePrimeGenerator(gLo);
			if (noGen) {
				gLo = gLo.subtract(BigInteger.ONE);
				gHi = gHi.add(BigInteger.ONE);
			}
		}
		while(noGen && ! gLo.equals(BigInteger.ONE)) {
			hiGen = this.verifyBasePrimeGenerator(gHi);
			if (hiGen) {
				noGen = false;
			} else {
				noGen = ! this.verifyBasePrimeGenerator(gLo);
				if (noGen) {
					gLo   = gLo.subtract(BigInteger.ONE);
					gHi   = gHi.add(BigInteger.ONE);
				}
			}
		}
		if (noGen) {
			throw new IllegalStateException(
				String.format("No generator could be found for <%s>", this.p.toString())
			);
		}

		return this.getPowerPrimeSequence(hiGen ? gHi : gLo, BigInteger.ONE, true);
	}

	public LongGroupGeneratorSpliterator getPowerPrimeSequence(final BigInteger g) {
		return this.getPowerPrimeSequence(g, BigInteger.ONE, false);
	}

	public LongGroupGeneratorSpliterator getPowerPrimeSequence(
		final BigInteger g, final BigInteger exp
	) {
		return this.getPowerPrimeSequence(g, exp, true);
	}

	public LongGroupGeneratorSpliterator getPowerPrimeSequence(
		final BigInteger g, boolean trustGGeneratesP
	) {
		return this.getPowerPrimeSequence(g, BigInteger.ONE, trustGGeneratesP);
	}

	/**
	 * Returns a stream that contains all (p^n - p - 2) integers that are 
	 * greater than 1, less than p^n, and not divisible by p.
	 * 
	 * Requires a prime, an exponent, and a generator for the multiplicative
	 * @param g
	 * @param p
	 * @param n
	 * @return
	 */
	public LongGroupGeneratorSpliterator getPowerPrimeSequence(
		final BigInteger g, final BigInteger exp, boolean trustGGeneratesP
	) {
		if (! (trustGGeneratesP || this.verifyBasePrimeGenerator(g))) {
			throw new IllegalArgumentException(
				String.format("<%s> is not a generator for <%s>", g, this.p));
		}

		final int pnBits = this.pn.bitLength();
		if (pnBits > 63) {
			throw new IllegalArgumentException(
				"Power of prime cannot exceed 64 bits in length for a LongStream"
			);
		}
		final int multBits = this.pn.subtract(BigInteger.ONE)
			.multiply(g)
			.bitLength();

        final LongGroupGeneratorSpliterator iterator;
		if (multBits <= 63) {
	        iterator = new PrimitiveGroupGeneratorSequence(
	        		this.pn.longValueExact(),
	        		this.p.longValueExact(), 
	        		g.longValueExact(),
	        		exp.longValueExact());
		} else {
	        iterator = new BigLongGroupGeneratorSequence(
	        		this.pn, this.p, g, exp);
		}

	    return iterator;
	}
}