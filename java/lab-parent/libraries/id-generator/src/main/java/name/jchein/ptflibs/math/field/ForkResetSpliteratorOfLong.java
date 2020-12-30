package name.jchein.ptflibs.math.field;

import java.util.Spliterator;

public interface ForkResetSpliteratorOfLong extends Spliterator.OfLong {
	ForkResetSpliteratorOfLong fork();
	
	ForkResetSpliteratorOfLong reset();
}
