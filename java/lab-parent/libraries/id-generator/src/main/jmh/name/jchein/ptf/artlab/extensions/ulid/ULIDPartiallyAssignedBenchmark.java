/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2017 Joern Huxhorn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright 2007-2017 Joern Huxhorn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package name.jchein.ptf.artlab.extensions.ulid;

import java.time.Clock;
import java.time.Duration;
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

import name.jchein.ptflibs.identity.ulid.AbstractULIDRandomBitsStrategy;
import name.jchein.ptflibs.identity.ulid.MonotonicULIDFactory;
import name.jchein.ptflibs.identity.ulid.SimpleULIDFactory;
import name.jchein.ptflibs.identity.ulid.ULID;
import name.jchein.ptflibs.identity.ulid.ULIDFactory;

@State(Scope.Thread)
public class ULIDPartiallyAssignedBenchmark {
	private final long RESERVED_ID = 0x81f3b5ac60L;

	private final Clock systemClock =
		Clock.systemUTC();
	private final Clock tickClock =
		Clock.tick(systemClock, Duration.ofMillis(2));
	private final AbstractULIDRandomBitsStrategy randomness = 
		new AbstractULIDRandomBitsStrategy(RESERVED_ID, 50, (byte) 2, 43, 18, 16);
//	private final FixedUDIDRandomBitsStrategy randomness = 
//		new FixedUDIDRandomBitsStrategy(0x93287, 0x8237972L, 0x8923L, 0x7aba39L);
	private final ULIDFactory simpleFactory =
		new SimpleULIDFactory(tickClock, randomness);
	private final ULIDFactory monotonicFactory =
		new MonotonicULIDFactory(tickClock, randomness);
	private final ULID fixedULID = simpleFactory.nextULID();
	

//	@Benchmark
//	@BenchmarkMode(Mode.Throughput)
//	@OutputTimeUnit(TimeUnit.MILLISECONDS)
//	public StringBuilder simpleAppendULIDThroughput() {
//		final StringBuilder buffer = new StringBuilder(26);
//		monotonicFactory.appendULID(buffer);
//		return buffer;
//	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public StringBuilder simpleAppendULIDAverage() {
		final StringBuilder buffer = new StringBuilder(26);
		monotonicFactory.appendULID(buffer);
		return buffer;
	}

//	@Benchmark
//	@BenchmarkMode(Mode.Throughput)
//	@OutputTimeUnit(TimeUnit.MILLISECONDS)
//	public String simpleNextULIDStringThroughput() {
//		return monotonicFactory.nextULIDString();
//	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public String simpleNextULIDStringAverage() {
		return monotonicFactory.nextULIDString();
	}

//	@Benchmark
//	@BenchmarkMode(Mode.Throughput)
//	@OutputTimeUnit(TimeUnit.MILLISECONDS)
//	public ULID simpleNextULIDThroughput() {
//		return monotonicFactory.nextULID();
//	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public ULID simpleNextULIDAverage() {
		return monotonicFactory.nextULID();
	}

//	@Benchmark
//	@BenchmarkMode(Mode.Throughput)
//	@OutputTimeUnit(TimeUnit.MILLISECONDS)
//	public ULID simpleAltNextULIDThroughput() {
//		return monotonicFactory.altNextULID();
//	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public ULID simpleAltNextULIDAverage() {
		return monotonicFactory.altNextULID();
	}

//	@Benchmark
//	@BenchmarkMode(Mode.Throughput)
//	@OutputTimeUnit(TimeUnit.MILLISECONDS)
//	public String simpleNextULIDToStringThroughput() {
//		return monotonicFactory.nextULID().toString();
//	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public String simpleNextULIDToStringAverage() {
		return monotonicFactory.nextULID().toString();
	}

//	@Benchmark
//	@BenchmarkMode(Mode.Throughput)
//	@OutputTimeUnit(TimeUnit.MILLISECONDS)
//	public String simpleAltNextULIDToStringThroughput() {
//		return monotonicFactory.altNextULID().toString();
//	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public String simpleAltNextULIDToStringAverage() {
		return monotonicFactory.altNextULID().toString();
	}

//	@Benchmark
//	@BenchmarkMode(Mode.Throughput)
//	@OutputTimeUnit(TimeUnit.MILLISECONDS)
//	public String simpleULIDToStringThroughput() {
//		return fixedULID.toString();
//	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public String simpleULIDToStringAverage() {
		return fixedULID.toString();
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder().include(
			ULIDPartiallyAssignedBenchmark.class.getSimpleName()
		)
			.warmupIterations(5)
			.measurementIterations(5)
			.forks(1)
			.build();

		new Runner(opt).run();
	}
}
