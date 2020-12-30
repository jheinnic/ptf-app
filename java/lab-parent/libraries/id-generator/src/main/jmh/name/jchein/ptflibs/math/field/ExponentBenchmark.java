package name.jchein.ptflibs.math.field;

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
public class ExponentBenchmark {

	private final int base = 103;
	private final int exp = 5;

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public long base103Exp5Math_TP() {
		return (long) Math.pow(base, exp);
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public long base103Exp5Math_AT() {
		return (long) Math.pow(base, exp);
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public long base103Exp5Loop_TP() {
		long retVal = base;
		for (int ii = 1; ii < exp; ii++) {
			retVal *= base;
		}
		return retVal;
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public long base103Exp5Loop_AT() {
		long retVal = base;
		for (int ii = 1; ii < exp; ii++) {
			retVal *= base;
		}
		return retVal;
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public long base103Exp5InlineBulk_TP() {
		final long retVal = base;
		return retVal * retVal * retVal * retVal * retVal;
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public long base103Exp5InlineBulk_AT() {
		final long retVal = base;
		return retVal * retVal * retVal * retVal * retVal;
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public long base103Exp5InlineSteps_TP() {
		long retVal = base;
		retVal *= base;
		retVal *= base;
		retVal *= base;
		retVal *= base;
		return retVal;
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public long base103Exp5InlineSteps_AT() {
		long retVal = base;
		retVal *= base;
		retVal *= base;
		retVal *= base;
		retVal *= base;
		return retVal;
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder().include(ExponentBenchmark.class.getSimpleName()).warmupIterations(5)
				.measurementIterations(5).forks(1).build();

		new Runner(opt).run();
	}
}
