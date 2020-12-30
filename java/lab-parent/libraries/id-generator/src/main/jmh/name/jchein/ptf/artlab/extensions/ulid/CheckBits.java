package name.jchein.ptf.artlab.extensions.ulid;

import java.math.BigInteger;
import java.security.SecureRandom;

public class CheckBits {

	public static void main(String[] args) {
		int twoBitsA = 17;
		int twoBitsB = 48;
		int flipA = ~twoBitsA;
		int flipB = ~twoBitsB;
		System.out.println(
			String.format("<%d> <%d>", twoBitsA, twoBitsB));
		System.out.println(
			String.format("<%d> <%d>", flipA, flipB));
		
		// 201919260
		int[] r = { 844165, 844165, 0, 0, 0, 0 };
		r[2] = r[1] |= (r[1] >>  1);
        r[3] = r[2] |= (r[2] >>  2);
        r[4] = r[3] |= (r[3] >>  4);
        r[5] = r[4] |= (r[4] >>  8);
        r[5] |= (r[5] >> 16);
        System.out.println(
        	String.format(
        		"<%d>, <%d>, <%d>, <%d>, <%d>, <%d>, <%d>",
        		r[0], r[1], r[2], r[3], r[4], r[5], r[5] >>> 1)
        );
		System.out.println(
			String.format("<%d>", Integer.highestOneBit(844165))
		);
		System.out.println(
			String.format("<%d>", Integer.numberOfLeadingZeros(844165))
		);
		System.out.println(
			String.format("<%d>", Integer.numberOfLeadingZeros(201919260))
		);
		System.out.println(
			String.format("<%d>", 32 - Integer.numberOfLeadingZeros(201260))
		);
		System.out.println(
			String.format("<%d>", 32 - Integer.numberOfLeadingZeros(91247))
		);
		System.out.println(
			String.format("<%d>", 32 - Integer.numberOfLeadingZeros(201260 * 91247))
		);
		
		System.out.println(
			String.format("<%s>", BigInteger.probablePrime(40, new SecureRandom()))
		);
		System.out.println(
			String.format("<%s>", BigInteger.probablePrime(7, new SecureRandom()))
		);
		BigInteger bi = BigInteger.valueOf(201919260);
		BigInteger nbi = BigInteger.valueOf(-201919260);
		System.out.println(
			String.format("<%d> <%d>", bi.bitLength(), bi.bitCount())
		);
		System.out.println(
			String.format("<%d> <%d>", nbi.bitLength(), nbi.bitCount())
		);
		System.out.println(
			String.format("<%s>",
				bi.subtract(
					BigInteger.valueOf(201919259)
				).equals(BigInteger.ONE)
			)
		);
		System.out.println(
			String.format("<%d> <%d> <%d>", ((0x1L << 64) - 1), (0x1L << 64), (0x1L << 63) - 1)
		);
		System.out.println(
			String.format("<%d>", BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE).longValue())
		);
		System.out.println(
			String.format("<%s>", BigInteger.valueOf(
				BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE).longValue()
			))
		);
		System.out.println(
			String.format("<%d>", BigInteger.ONE.shiftLeft(63))
		);
		System.out.println(
			String.format("<%d>", BigInteger.ONE.shiftLeft(63).longValue())
		);
		System.out.println(
			String.format("<%d>", BigInteger.ONE.shiftLeft(63).longValueExact())
		);
	}

}
