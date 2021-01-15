package name.jchein.ptflibs.math.field;

import java.math.BigInteger;
import java.util.Spliterator;

public interface BigIntGroupGeneratorSpliterator extends Spliterator<BigInteger> {
	BigIntGroupGeneratorSpliterator fork();
	
	BigIntGroupGeneratorSpliterator reset();
}
