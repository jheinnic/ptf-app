package name.jchein.ptflibs.math.field;

import java.math.BigInteger;
import java.util.Spliterator;

public interface ForkResetSpliteratorOfBigInteger extends Spliterator<BigInteger> {
	ForkResetSpliteratorOfBigInteger fork();
	
	ForkResetSpliteratorOfBigInteger reset();
}
