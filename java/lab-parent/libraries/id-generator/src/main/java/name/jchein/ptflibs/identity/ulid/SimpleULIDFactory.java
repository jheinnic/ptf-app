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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import javax.validation.constraints.NotNull;

/*
 * https://github.com/ulid/spec
 */
// @SuppressWarnings("PMD.ShortClassName")
public class SimpleULIDFactory implements ULIDFactory
{
	protected final Clock clock;
	protected final ULIDRandomBitsStrategy random;

	public SimpleULIDFactory()
	{
		this(
			Clock.systemUTC(),
			new AbstractULIDRandomBitsStrategy(
				0x10L, 4471, (byte) 7, 2, 63, 12
			)
		);
	}

	public SimpleULIDFactory(@NotNull Clock clock)
	{
		this(
			clock,
			new AbstractULIDRandomBitsStrategy(
				0x10L, 4471, (byte) 7, 2, 63, 12
			)
		);
	}

	public SimpleULIDFactory(@NotNull ULIDRandomBitsStrategy random)
	{
		this(
			Clock.systemUTC(), random
		);
	}

	public SimpleULIDFactory(@NotNull Clock clock, @NotNull ULIDRandomBitsStrategy random)
	{
		Objects.requireNonNull(clock, "clock must not be null!");
		Objects.requireNonNull(random, "random must not be null!");
		this.clock = clock;
		this.random = random;
	}

	public String nextULIDString()
	{
		final StringBuilder builder = new StringBuilder(26);
		appendULID(builder);
		return builder.toString();
	}
	
	public void appendULID(StringBuilder builder) {
		Objects.requireNonNull(builder, "builder must not be null!");
		this.random.onBackTick4040((final long hi40, final long lo40) -> {
			ULID.encodeTimeHi40Lo40(
				builder, this.clock.millis(), hi40, lo40);
		});
	}

//	public String nextULIDPath(char separator)
//	{
//		return NewULID.internalULIDString(
//			this.clock.millis(),
//			this.random.getRandomHi40(),
//			this.random.getRandomLo40());
//	}
//	
//	public void appendULIDPath(StringBuilder buffer, char separator) {
//		NewULID.internalAppendULID(
//			buffer, 
//			this.clock.millis(),
//			this.random.getRandomHi40(),
//			this.random.getRandomLo40());
//	}

	public ULID altNextULID()
	{
		CompletableFuture<ULID> retVal = new CompletableFuture<>();
		final long timestamp = this.clock.millis();
		this.random.onBackTick4040((final long hi40, final long lo40) -> {
			retVal.complete(
				ULID.fromTimeHi40Lo40(timestamp, hi40, lo40));
			});
		try {
			return retVal.get();
		} catch( Exception e ) {
			throw new RuntimeException(e);
		}
	}
	
	public ULID nextULID()
	{
		CompletableFuture<ULID> retVal = new CompletableFuture<>();
		final long timestamp = this.clock.millis();
		this.random.onBackTickIntLong((final int hi16, final long lo64) -> {
			retVal.complete(
				ULID.fromTimeHi16Lo64(timestamp, hi16, lo64));
			});
		try {
			return retVal.get();
		} catch( Exception e ) {
			throw new RuntimeException(e);
		}
	}
}
