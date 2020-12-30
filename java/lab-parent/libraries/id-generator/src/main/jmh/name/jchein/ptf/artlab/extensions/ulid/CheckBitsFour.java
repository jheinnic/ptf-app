package name.jchein.ptf.artlab.extensions.ulid;

import java.util.Arrays;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import name.jchein.ptflibs.math.field.ForkResetSpliteratorOfLong;
import name.jchein.ptflibs.math.field.PrimePowerField;

@State(Scope.Thread)
public class CheckBitsFour {

	public static void main(String[] args) {
		final PrimePowerField bigPpf = PrimePowerField.generateField(48, 6, 18);
		final ForkResetSpliteratorOfLong series = bigPpf.findBasePrimeGenerator();

		long[] sequence = new long[12];
		for (int ii = 0; ii < 12; ii++) {
			final int idx = ii;
			series.tryAdvance((long nextLong) -> {
				sequence[idx] = nextLong;
			});
		}
		System.out.println(
			String.format("<%s>", Arrays.toString(sequence)));
	}
}
