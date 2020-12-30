package name.jchein.ptflibs.math.field;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
public class PrimePowerFieldBenchmark {
//	private final int base1 = 103;
//	private final int exp1 = 5;
	private final int base2 = 97;
	private final BigInteger g2 = BigInteger.valueOf(41);
	private final int exp2 = 3;

	private final PrimePowerField ppf2 = PrimePowerField.getField(BigInteger.valueOf(base2), exp2);
	
	private final PrimePowerField bigPpf =
		PrimePowerField.generateField(48, 25, 50);

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public long iter2_TP() {
		final ForkResetSpliteratorOfLong iter = bigPpf.getPowerPrimeSequence(g2);
		final long[] nextHolder = new long[1];
		iter.tryAdvance((long next) -> {
			if (next <= 0) {
				throw new IllegalStateException("Negative");
			}

			nextHolder[0] = next;
		});
		System.out.println(String.format("<%d>", nextHolder[0]));
		return nextHolder[0];
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public long iter2_AT() {
		final ForkResetSpliteratorOfLong iter = bigPpf.getPowerPrimeSequence(g2);
		final long[] nextHolder = new long[1];
		iter.tryAdvance((long next) -> {
			if (next <= 0) {
				throw new IllegalStateException("Negative");
			}

			nextHolder[0] = next;
		});
		System.out.println(String.format("<%d>", nextHolder[0]));
		return nextHolder[0];
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder().include(PrimePowerFieldBenchmark.class.getSimpleName()).warmupIterations(5)
				.measurementIterations(5).forks(1).build();

		new Runner(opt).run();
	}
}