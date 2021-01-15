package name.jchein.ptflibs.math.field;

import java.util.Spliterator;

/**
 * Ordered Spliterator of long values where the sequence value range and
 * length is determined by a given prime number raised to some exponnent
 * and a "generator" value that returns every value in the group of 
 * integers modulo the base prime number.
 * 
 * When a generator for the group of integers modulo prime is applied to
 * the integers modulo an exponential power of the base prime, it can be
 * shown that the same generator for the entire set of integers module
 * prime will also generate the all integers module the prime power, 
 * excluding those integers that are multiples of the original prime.
 * 
 * For a prime number p, this yields a pseudo-random unique ordering of
 * (p^n) - (p^(n-1)) distinct values (x) from the range 1 <= x < (p^n). 
 * Elements from this range that are excluded are the p^(n-1) multiples
 * of p, satisfying the formula b*p for 1 <= b < p
 *
 * Above and beyond access to the order of elements in such a sequence,
 * implementations provide some additional features:
 * <ul>
 * <li>Access to values that define the parameters required to define
 *     which of all the many conforming sequences is actively in use</li>
 * <li>Access to exponent that effectively indexes the spliterator's
 *     current position in sequence</li>
 * <li>Access to number of downstream elements still remaining beyond
 *     current element</li>
 * <li>A forking function that returns a new Spliterator over the same
 *     sequence, but with its own unique sense of position in that list,
 *     which is initially set to the same point as the original.</li>
 * <li>A reset function that returns a new Spliterator rewound to the
 *     same point where the most recently fork()ed sequence began</li>
 * </ul>
 */
public interface LongGroupGeneratorSpliterator extends Spliterator.OfLong {
	LongGroupGeneratorSpliterator fork();
	
	LongGroupGeneratorSpliterator reset();
	
	long getP();
	
	long getPN();
	
	long getG();
	
	long getT();
	
	long getExponent();
	
	long getRemaining();
}
