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

import java.security.SecureRandom;
import java.time.Clock;
import java.util.Objects;

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
			new RandomBasedRandomBits(
				new SecureRandom()
			)
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
		appendULIDString(builder);
		return builder.toString();
	}
	
	public void appendULIDString(StringBuilder builder) {
		Objects.requireNonNull(builder, "builder must not be null!");
		ULID.internalEncodeTimeHi40Lo40(
			builder, 
			this.clock.millis(),
			this.random.getRandomHi40(),
			this.random.getRandomLo40());
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
		return ULID.fromTimeHi40Lo40(
			this.clock.millis(),
			this.random.getRandomHi40(),
			this.random.getRandomLo40());
	}
	
	public ULID nextULID()
	{
		return ULID.fromTimeHi16Lo64(
			this.clock.millis(),
			this.random.getRandomHi16(),
			this.random.getRandomLo64());
	}
}
