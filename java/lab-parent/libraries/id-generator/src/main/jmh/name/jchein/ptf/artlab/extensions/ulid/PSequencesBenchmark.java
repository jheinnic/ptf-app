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

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
import org.pcollections.ConsPStack;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

@State(Scope.Thread)
public class PSequencesBenchmark {

	static Long[] buildElementList() {
		Random random = new SecureRandom();
		Long[] retVal = new Long[20];
		for (int ii = 0; ii < 20; ii++) {
			retVal[ii] = random.nextLong();
		}
		return retVal;
	}

	static ArrayList<Long> shuffleElementList(List<Long> source) {
		ArrayList<Long> retVal = new ArrayList<Long>(source);
		Collections.shuffle(retVal);
		return retVal;
	}

	private final Long[] rawElements = buildElementList();

	private final List<Long> sameElements = Arrays.stream(rawElements).collect(Collectors.toList());

	private final ArrayList<Long> shuffledElements = shuffleElementList(sameElements);

	private final ConsPStack<Long> prebuiltStack =
		ConsPStack.<Long>from(sameElements);

	private final TreePVector<Long> prebuiltVector =
		TreePVector.<Long>from(sameElements);

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.SECONDS)
	public PSequence<Long> buildConsPStack_20Plus() {
		final PSequence<Long> buildMe = ConsPStack.<Long>empty();
		return buildMe.plus(rawElements[0]).plus(rawElements[1]).plus(rawElements[2]).plus(rawElements[3])
			.plus(rawElements[4]).plus(rawElements[5]).plus(rawElements[6]).plus(rawElements[7])
			.plus(rawElements[8]).plus(rawElements[9]).plus(rawElements[10]).plus(rawElements[11])
			.plus(rawElements[12]).plus(rawElements[13]).plus(rawElements[14]).plus(rawElements[15])
			.plus(rawElements[16]).plus(rawElements[17]).plus(rawElements[18]).plus(rawElements[19]);
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.SECONDS)
	public PSequence<Long> buildConsPStack_Bulk() {
		PSequence<Long> buildMe = ConsPStack.<Long>from(sameElements);
		return buildMe;
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.SECONDS)
	public PSequence<Long> buildConsPStack_RandomDeleteOrder() {
		PSequence<Long> buildMe = ConsPStack.<Long>from(sameElements);
		return buildMe.minus(shuffledElements.get(0)).minus(shuffledElements.get(1))
				.minus(shuffledElements.get(2)).minus(shuffledElements.get(3))
				.minus(shuffledElements.get(4)).minus(shuffledElements.get(5))
				.minus(shuffledElements.get(6)).minus(shuffledElements.get(7))
				.minus(shuffledElements.get(8)).minus(shuffledElements.get(9))
				.minus(shuffledElements.get(10)).minus(shuffledElements.get(11))
				.minus(shuffledElements.get(12)).minus(shuffledElements.get(13))
				.minus(shuffledElements.get(14)).minus(shuffledElements.get(15))
				.minus(shuffledElements.get(16)).minus(shuffledElements.get(17))
				.minus(shuffledElements.get(18)).minus(shuffledElements.get(19));
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.SECONDS)
	public Long refConsPStack_5and10() {
	    return prebuiltStack.get(5) + prebuiltStack.get(15);
	}

//	@Benchmark
//	@BenchmarkMode(Mode.AverageTime)
//	@OutputTimeUnit(TimeUnit.NANOSECONDS)
//	public PSequence<Long> avgBuildConsPStack_20Plus() {
//		PSequence<Long> buildMe = ConsPStack.<Long>empty();
//		buildMe = buildMe.plus(rawElements[0]);
//		buildMe = buildMe.plus(rawElements[1]);
//		buildMe.plus(rawElements[2]).plus(rawElements[3]).plus(rawElements[4]).plus(rawElements[5]).plus(rawElements[6])
//				.plus(rawElements[7]).plus(rawElements[8]).plus(rawElements[9]).plus(rawElements[10])
//				.plus(rawElements[11]).plus(rawElements[12]).plus(rawElements[13]).plus(rawElements[14])
//				.plus(rawElements[15]).plus(rawElements[16]).plus(rawElements[17]).plus(rawElements[18])
//				.plus(rawElements[19]);
//		return buildMe;
//	}

//	@Benchmark
//	@BenchmarkMode(Mode.AverageTime)
//	@OutputTimeUnit(TimeUnit.SECONDS)
//	public PSequence<Long> avgBuildConsPStack_Bulk() {
//		PSequence<Long> buildMe = ConsPStack.<Long>from(sameElements);
//		return buildMe;
//	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.SECONDS)
	public PSequence<Long> buildTreePVector_20Plus() {
		PSequence<Long> buildMe = TreePVector.<Long>empty();
		buildMe = buildMe.plus(rawElements[0]);
		buildMe = buildMe.plus(rawElements[1]);
		buildMe.plus(rawElements[2]).plus(rawElements[3]).plus(rawElements[4]).plus(rawElements[5]).plus(rawElements[6])
				.plus(rawElements[7]).plus(rawElements[8]).plus(rawElements[9]).plus(rawElements[10])
				.plus(rawElements[11]).plus(rawElements[12]).plus(rawElements[13]).plus(rawElements[14])
				.plus(rawElements[15]).plus(rawElements[16]).plus(rawElements[17]).plus(rawElements[18])
				.plus(rawElements[19]);
		return buildMe;
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.SECONDS)
	public PSequence<Long> buildTreePVector_Bulk() {
		PSequence<Long> buildMe = TreePVector.<Long>from(sameElements);
		return buildMe;
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.SECONDS)
	public PSequence<Long> buildTreePStack_RandomDeleteOrder() {
		PSequence<Long> buildMe = ConsPStack.<Long>from(sameElements);
		return buildMe.minus(shuffledElements.get(0)).minus(shuffledElements.get(1))
				.minus(shuffledElements.get(2)).minus(shuffledElements.get(3))
				.minus(shuffledElements.get(4)).minus(shuffledElements.get(5))
				.minus(shuffledElements.get(6)).minus(shuffledElements.get(7))
				.minus(shuffledElements.get(8)).minus(shuffledElements.get(9))
				.minus(shuffledElements.get(10)).minus(shuffledElements.get(11))
				.minus(shuffledElements.get(12)).minus(shuffledElements.get(13))
				.minus(shuffledElements.get(14)).minus(shuffledElements.get(15))
				.minus(shuffledElements.get(16)).minus(shuffledElements.get(17))
				.minus(shuffledElements.get(18)).minus(shuffledElements.get(19));
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.SECONDS)
	public Long refTreePVector_5and10() {
	    return prebuiltVector.get(5) + prebuiltVector.get(15);
	}

//	@Benchmark
//	@BenchmarkMode(Mode.AverageTime)
//	@OutputTimeUnit(TimeUnit.NANOSECONDS)
//	public PSequence<Long> avgBuildTreePVector_20Plus() {
//		PSequence<Long> buildMe = TreePVector.<Long>empty();
//		buildMe = buildMe.plus(rawElements[0]);
//		buildMe = buildMe.plus(rawElements[1]);
//		buildMe.plus(rawElements[2]).plus(rawElements[3]).plus(rawElements[4]).plus(rawElements[5]).plus(rawElements[6])
//				.plus(rawElements[7]).plus(rawElements[8]).plus(rawElements[9]).plus(rawElements[10])
//				.plus(rawElements[11]).plus(rawElements[12]).plus(rawElements[13]).plus(rawElements[14])
//				.plus(rawElements[15]).plus(rawElements[16]).plus(rawElements[17]).plus(rawElements[18])
//				.plus(rawElements[19]);
//		return buildMe;
//	}
//
//	@Benchmark
//	@BenchmarkMode(Mode.AverageTime)
//	@OutputTimeUnit(TimeUnit.SECONDS)
//	public PSequence<Long> avgBuildTreePVector_Bulk() {
//		PSequence<Long> buildMe = TreePVector.<Long>from(sameElements);
//		return buildMe;
//	}
//
//	@Benchmark
//	@BenchmarkMode(Mode.AverageTime)
//	@OutputTimeUnit(TimeUnit.SECONDS)
//	public PSequence<Long> avgBuildTreePVector_RandomDeleteOrder() {
//		PSequence<Long> buildMe = TreePVector.<Long>from(sameElements);
//		return buildMe;
//	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder().include(PSequencesBenchmark.class.getSimpleName()).warmupIterations(5)
				.measurementIterations(5).forks(1).build();

		new Runner(opt).run();
	}
}
