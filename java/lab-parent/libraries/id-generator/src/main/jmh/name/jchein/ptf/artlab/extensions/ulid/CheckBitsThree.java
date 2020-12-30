package name.jchein.ptf.artlab.extensions.ulid;

import java.math.BigInteger;
import java.security.SecureRandom;

public class CheckBitsThree {

	public static void main(String[] args) {
		final int target = 21;
		final int minExp = 8;
		final int maxExp = 20;
		BigInteger max = BigInteger.ONE.shiftLeft(target)
			.subtract(BigInteger.ONE);
		BigInteger bestDist = max;
		BigInteger best = max;
		int bestBits = 0;
		int bestExp = 0;
		for (int jj=minExp; jj<=maxExp; jj++) {
			int firstExp = target / jj;
			for (int ii=0; ii<100; ii++) {
				BigInteger prime = BigInteger.probablePrime(
					jj, new SecureRandom());
				int thisExp = firstExp;
				int nextExp = firstExp + 1;
				BigInteger power = prime.pow(firstExp);
				BigInteger nextPower = prime.pow(nextExp);
				BigInteger distance = max.subtract(power);
				for(
					BigInteger nextDistance = max.subtract(nextPower);
					nextDistance.signum() > 0;
					nextDistance = max.subtract(nextPower)
				) {
					thisExp = nextExp;
					power = nextPower;
					distance = nextDistance;
					nextExp = nextExp + 1;
					nextPower = prime.pow(nextExp);
				}
				System.out.println(
					String.format(
						"Generated <%d> bits, from <%d> prime bits, exponent <%d> :: <%s>, with distance <%s>",
						power.bitLength(),
						jj,
						thisExp,
						prime,
						distance
					)
				);
				if ((distance.signum() > -1) && (bestDist.subtract(distance).signum() > 0)) {
					bestDist = distance;
					best = prime;
					bestExp = thisExp;
					bestBits = jj;
					System.out.println("** New Best **");
				}
			}
		}
		System.out.println(
			String.format(
				"Best: <%d> bits, <%d> exponent, <%s> prime, <%s> distance",
				bestBits, bestExp, best, bestDist
			)
		);
	}
}
