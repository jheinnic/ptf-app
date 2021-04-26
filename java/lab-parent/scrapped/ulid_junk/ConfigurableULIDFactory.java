package name.jchein.ptflibs.identity.ulid.junk;
///*
// * sulky-modules - several general-purpose modules.
// * Copyright (C) 2007-2019 Joern Huxhorn
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Lesser General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//
///*
// * Copyright 2007-2019 Joern Huxhorn
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package name.jchein.ptflibs.identity.ulid;
//
//import java.security.SecureRandom;
//import java.time.Clock;
//import java.time.Duration;
//import java.util.Objects;
//
//import javax.validation.constraints.NotNull;
//
//import lombok.Builder;
//import lombok.Value;
//import lombok.experimental.Wither;
//
///*
// * https://github.com/ulid/spec
// */
////@SuppressWarnings("PMD.ShortClassName")
//public class ConfigurableULIDFactory extends SimpleULIDFactory
//{
//	@Value
//	@Builder
//	static class ULIDFactoryState {
//		@Wither
//		final Clock clock;
//		@Wither
//		final Duration tick;
//		@Wither
//		final int shortTermTicks;
//		@Wither
//		final ULIDRandomBitsStrategy random;
//		@Wither
//		final BehaviorOnExhaustion onExhaustion;
//		@Wither
//		final BehaviorOnExhaustion onShortExhaustion;
//		@Wither
//		final ConflictAvoidanceMode conflictMode;
//		@Wither
//		final int monoBitCount;
//		@Wither
//		final int monoZeroedBits;
//		final long monoBitMask;
//		final long monoZeroMask;
//	}
//	
//	ULIDFactoryState currentState;
//	
//	class ULIDFactoryConfigBuilderImpl implements ULIDFactoryConfigBuilder {
//		Clock nextClock;
//		Duration nextTick;
//		int nextShortTermTicks;
//		ULIDRandomBitsStrategy nextRandom;
//		BehaviorOnExhaustion nextOnExhaustion;
//		BehaviorOnExhaustion nextOnShortExhaustion;
//		ConflictAvoidanceMode nextConflictMode;
//		int nextMonoBitCount;
//		int nextMonoZeroedBits;
//		
//		public ULIDFactoryConfigBuilderImpl() {
//			this.nextClock = currentState.clock;
//			this.nextTick = currentState.tick;
//			this.nextRandom = currentState.random;
//			this.nextOnExhaustion = currentState.onExhaustion;
//			this.nextOnShortExhaustion = currentState.onShortExhaustion;
//			this.nextShortTermTicks = currentState.shortTermTicks;
//			this.nextConflictMode = currentState.conflictMode;
//			this.nextMonoBitCount = currentState.monoBitCount;
//			this.nextMonoZeroedBits = currentState.monoZeroedBits;
//		}
//
//		public ULIDFactoryConfigBuilder setClock(final Clock clock, final Duration tick, final int shortTermTicks) {
//			this.nextClock = clock;
//			this.nextTick = tick;
//			this.nextShortTermTicks = shortTermTicks;
//			return this;
//		}
//	    
//	    public ULIDFactoryConfigBuilder setRandom(final ULIDRandomBitsStrategy random) {
//	    	this.nextRandom = random;
//	    	return this;
//	    }
//	    
//	    public ULIDFactoryConfigBuilder setOnExhaustion(final BehaviorOnExhaustion onExhaustion) {
//	    	this.nextOnExhaustion = onExhaustion;
//	    	return this;
//	    }
//
//	    public ULIDFactoryConfigBuilder setOnShortTermExhaustion(final BehaviorOnExhaustion onShortExhaustion) {
//	    	this.nextOnShortExhaustion = onShortExhaustion;
//	    	return this;
//	    }
//	    
//	    public ULIDFactoryConfigBuilder setWrapMonoMode(final int monoBitCount) {
//	    	this.nextConflictMode = ConflictAvoidanceMode.WRAP_MONO;
//	    	this.nextMonoBitCount = monoBitCount;
//	    	return this;
//	    }
//
//	    public ULIDFactoryConfigBuilder setStrictMonoMode(final int monoBitCount, final int monoZeroedBits) {
//	    	this.nextConflictMode = ConflictAvoidanceMode.WRAP_MONO;
//	    	this.nextMonoBitCount = monoBitCount;
//	    	this.nextMonoZeroedBits = monoZeroedBits;
//	    	return this;
//	    }
//	    
//	    public ULIDFactoryConfigBuilder disableClockTracking() {
//	    	this.nextConflictMode = ConflictAvoidanceMode.NO_TRACKING;
//	    	return this;
//	    }
//	}
//	protected ULID latestValue;
//
//	public ConfigurableULIDFactory()
//	{
//		this(
//			Clock.systemUTC(),
//			new RandomBasedRandomBits(
//				new SecureRandom()
//			),
//			new ULID(0, 0)
//		);
//		Clock.tick()
//	}
//	
//	public ConfigurableULIDFactory(@NotNull Clock clock, @NotNull ULIDRandomBitsStrategy random) {
//		this(clock, random, new ULID(0, 0));
//	}
//
//	public ConfigurableULIDFactory(
//		@NotNull Clock clock, @NotNull ULIDRandomBitsStrategy random, @NotNull ULID latestValue
//	) {
//		super(clock, random);
//		Objects.requireNonNull(latestValue, "latestValue must not be null!");
//		this.latestValue = latestValue;
//	}
//
//	public void appendULID(StringBuilder builder)
//	{
//		Objects.requireNonNull(builder, "stringBuilder must not be null!");
//		ULID nextULID = nextULID();
//		ULID.internalEncodeULID(
//			builder,
//			nextULID.getMostSignificantBits(),
//			nextULID.getLeastSignificantBits()
//		);
//	}
//
//	public ULID nextULID()
//	{
//		long timestamp = this.clock.millis();
//		long previousTimestamp = this.latestValue.timestamp();
//		if (timestamp <= previousTimestamp) {
//			long lsBits = this.latestValue.getLeastSignificantBits();
//			long msBits = this.latestValue.getMostSignificantBits();
//
//			if (lsBits != Constants.FULL_WORD_MASK) {
//				this.latestValue = new ULID(msBits, lsBits+1);
//			} else if((msBits & Constants.HI_RANDOM_16_MSB_MASK) != Constants.HI_RANDOM_16_MSB_MASK) {
//				this.latestValue = new ULID(msBits+1, 0);
//			} else {
//				// TODO: Log a warning about overflow!
//				this.latestValue = new ULID(0, 0);
//			}
//		} else {
//			this.latestValue = ULID.fromTimeHi16Lo64(
//				timestamp,
//				this.random.getRandomHi16(),
//				this.random.getRandomLo64()
//			);
//		}
//		
//		return this.latestValue;
//	}
//
//	public void in(StringBuilder builder)
//	{
//		Objects.requireNonNull(builder, "stringBuilder must not be null!");
//		long timestamp = this.clock.millis();
//		long previousTimestamp = this.latestValue.timestamp();
//		if (timestamp <= previousTimestamp) {
//			long lsBits = this.latestValue.getLeastSignificantBits();
//			long msBits = this.latestValue.getMostSignificantBits();
//
//			if (lsBits != Constants.FULL_WORD_MASK) {
//				ULID.internalEncodeULID(builder, msBits, lsBits + 1);
//			} else if((msBits & Constants.HI_RANDOM_16_MSB_MASK) != Constants.HI_RANDOM_16_MSB_MASK) {
//				ULID.internalEncodeULID(builder, msBits + 1, 0);
//			} else {
//				// TODO: Log a warning about overflow!
//				ULID.internalEncodeULID(builder, 0, 0);
//			}
//		} else {
//			ULID.internalEncodeTimeHi40Lo40(
//				builder,
//				timestamp, 
//				this.random.getRandomHi40(), 
//				this.random.getRandomLo40()
//			);
//		}
//	}
//}
