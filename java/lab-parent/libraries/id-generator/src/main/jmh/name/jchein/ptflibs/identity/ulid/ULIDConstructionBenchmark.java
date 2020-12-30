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

package name.jchein.ptflibs.identity.ulid;

import java.util.UUID;
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
public class ULIDConstructionBenchmark
{
	private final SimpleULIDFactory simpleStatelessFactory = new SimpleULIDFactory();
	//private final ULID randomFactory = new ULID(new Random());

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.SECONDS)
	public String simpleNextULIDStringThroughput()
	{
		return simpleStatelessFactory.nextULIDString();
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public String simpleNextULIDStringAverage()
	{
		return simpleStatelessFactory.nextULIDString();
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.SECONDS)
	public ULID simpleNextULIDThroughput()
	{
		return simpleStatelessFactory.nextULID();
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public ULID simpleAltNextULIDAverage()
	{
		return simpleStatelessFactory.altNextULID();
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.SECONDS)
	public ULID simpleAltNextULIDThroughput()
	{
		return simpleStatelessFactory.altNextULID();
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public ULID simpleNextULIDAverage()
	{
		return simpleStatelessFactory.nextULID();
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.SECONDS)
	public String simpleNextULIDToStringThroughput()
	{
		return simpleStatelessFactory.nextULID().toString();
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public String simpleNextULIDToStringAverage()
	{
		return simpleStatelessFactory.nextULID().toString();
	}


	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.SECONDS)
	public UUID uuidRandomUUIDThroughput()
	{
		return UUID.randomUUID();
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public UUID uuidRandomUUIDAverage()
	{
		return UUID.randomUUID();
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.SECONDS)
	public String uuidRandomUUIDToStringThroughput()
	{
		return UUID.randomUUID().toString();
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public String uuidRandomUUIDToStringAverage()
	{
		return UUID.randomUUID().toString();
	}

	public static void main(String[] args) throws RunnerException
	{
		Options opt = new OptionsBuilder()
				.include(ULIDConstructionBenchmark.class.getSimpleName())
				.warmupIterations(5)
				.measurementIterations(5)
				.forks(1)
				.build();

		new Runner(opt).run();
	}
}
