/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2019 Joern Huxhorn
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
 * Copyright 2007-2019 Joern Huxhorn
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

import java.time.Clock;

import javax.validation.constraints.NotNull;

/*
 * https://github.com/ulid/spec
 */
public class MonotonicULIDFactory implements ULIDFactory
{
	protected long latestTimestamp;
	protected final Clock clock;
	protected final ULIDRandomBitsStrategy random;

	public MonotonicULIDFactory()
	{
		this(
			Clock.systemUTC(),
			new AbstractULIDRandomBitsStrategy(
				0xae729bd4c0L, 8970, (byte) 2, 40, 24, 13
			)
		);
	}

	public MonotonicULIDFactory(@NotNull Clock clock)
	{
		this(
			clock,
			new AbstractULIDRandomBitsStrategy(
				0xae729bd4c0L, 8970, (byte) 2, 40, 24, 13
			)
		);
	}

	public MonotonicULIDFactory(@NotNull ULIDRandomBitsStrategy random)
	{
		this(
			Clock.systemUTC(), random
		);
	}

	
	public MonotonicULIDFactory(
		@NotNull Clock clock, @NotNull ULIDRandomBitsStrategy random
	) {
		this.clock = clock;
		this.random = random;
		this.latestTimestamp = 0;
	}


	public ULID altNextULID()
	{
		final ULID[] retVal = new ULID[1];
		final long timestamp = this.clock.millis();
		processULIDRequest(
			timestamp,
			(final long hi40, final long lo40) -> {
				retVal[0] = ULID.fromTimeHi40Lo40(timestamp, hi40, lo40);
			}
		);

		return retVal[0];
	}

	@Override
	public void appendULID(final StringBuilder builder)
	{
		final long timestamp = this.clock.millis();
		processULIDRequest(
			timestamp,
			(final long hi40, final long lo40) -> {
				ULID.encodeTimeHi40Lo40(builder, timestamp, hi40, lo40);
			}
		);
	}

	private void processULIDRequest(
		final long timestamp,
		final ULIDRandomBitsStrategy.Random4040Callback callback)
	{
		final long previousTimestamp = this.latestTimestamp;
		if (timestamp < previousTimestamp) {
			this.random.onBackTick4040(timestamp, callback);
		} else if (timestamp == previousTimestamp) {
			this.random.onSameTick4040(timestamp, callback);
		} else {
			this.random.onForwardTick4040(timestamp, callback);
		}
		this.latestTimestamp = timestamp;
	}

	public ULID nextULID()
	{
		long timestamp = this.clock.millis();
		ULID[] retVal = new ULID[1];
		if (timestamp < this.latestTimestamp) {
			// TODO: Treat time < prevTime as time == prevTime within a small
			//       window of tolerance where (prevTime - time) < tolerance,
			//       sparing sequence iteration when the interval to recovery
			//       is brief.
			// NOTE: We sort of have this already by setting a Clock interface
			//       tick size...  Maybe disregard this TODO?
			this.random.onBackTickIntLong(timestamp, (final int hi16, final long lo64) -> {
				retVal[0] = ULID.fromTimeHi16Lo64(timestamp, hi16, lo64);
			});
		} else if (timestamp == this.latestTimestamp) {
			this.random.onSameTickIntLong(timestamp, (final int hi16, final long lo64) -> {
				retVal[0] = ULID.fromTimeHi16Lo64(timestamp, hi16, lo64);
			});
		} else {
			// This will rewind random bits to the earliest reset point in
			// their sequence, which is only bumped forward when time has moved
			// backwards. We can reuse any node bits we do not believe we have
			// already used with an earlier occurence of present clock time.
			this.random.onForwardTickIntLong(timestamp, (final int hi16, final long lo64) -> {
				retVal[0] = ULID.fromTimeHi16Lo64(timestamp, hi16, lo64);
			});
		}
		// If clock has moved forward or not at all, this is intuitive.
		// If clock has moved backwards, this is Ok because we forced advancement to
		// the next time series and forced a reset of the reset point.
		// Note that if we move to other time series due to clock not moving, those
		// series advancements do NOT push the reset point forward by themselves--that
		// always requires a temporal backstep.
		this.latestTimestamp = timestamp;
		
//		try {
		return retVal[0];
//		} catch( Exception e ) {
//			throw new RuntimeException(e);
//		}
	}

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
}
