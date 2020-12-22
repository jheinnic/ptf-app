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

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.Size;

/*
 * https://github.com/ulid/spec
 */
public class ULID implements Comparable<ULID>, Serializable {
	private static final long serialVersionUID = -3563159514112487717L;

	/*
	 * The most significant 64 bits of this ULID.
	 */
	private final long mostSignificantBits;

	/*
	 * The least significant 64 bits of this ULID.
	 */
	private final long leastSignificantBits;

	ULID(long mostSignificantBits, long leastSignificantBits) {
		this.mostSignificantBits = mostSignificantBits;
		this.leastSignificantBits = leastSignificantBits;
	}

	/**
	 * Returns the most significant 64 bits of this ULID's 128 bit value.
	 *
	 * @return The most significant 64 bits of this ULID's 128 bit value
	 */
	public long getMostSignificantBits() {
		return mostSignificantBits;
	}

	/**
	 * Returns the least significant 64 bits of this ULID's 128 bit value.
	 *
	 * @return The least significant 64 bits of this ULID's 128 bit value
	 */
	public long getLeastSignificantBits() {
		return leastSignificantBits;
	}

	public long timestamp() {
		return mostSignificantBits >>> Constants.ULID_TO_CLOCK_SHIFT;
	}
	
	public long hiRandom16() {
		return (this.mostSignificantBits & Constants.HI_RANDOM_16_MSB_MASK);
	}
	
	public long hiRandom40() {
		return ((this.mostSignificantBits & Constants.HI_RANDOM_16_MSB_MASK) << Constants.HI_RANDOM_40_MSB_SHIFT) |
				(this.leastSignificantBits >>> Constants.HI_RANDOM_40_LSB_SHIFT);
	}
	
	public long loRandom40() {
		return this.leastSignificantBits & Constants.LO_RANDOM_40_LSB_MASK;
	}

	public byte[] toBytes() {
		final byte[] result = new byte[16];
		this.toBytes(result);
		return result;
	}

	public void toBytes(@Size(min = 16, max = 16) byte[] result) {
		long interim = mostSignificantBits;
		for (int i = 7; i > 0; i--) {
			result[i] = (byte) (interim & Constants.SINGLE_BYTE_MASK);
			interim >>= Constants.BYTE_BITS;
		}
		result[0] = (byte) interim;

		interim = leastSignificantBits;
		for (int i = 15; i > 8; i--) {
			result[i] = (byte) (interim & Constants.SINGLE_BYTE_MASK);
			interim >>= Constants.BYTE_BITS;
		}
		result[8] = (byte) interim;
	}


//	public NewULID increment() {
//		return increment(false);
//	}
//
//	public NewULID increment(boolean strict) {
//		long lsb = leastSignificantBits;
//		if (lsb != Constants.FULL_WORD_MASK) {
//			return new NewULID(mostSignificantBits, lsb + 1);
//		}
//		long msb = mostSignificantBits;
//		if ((msb & Constants.RANDOM_MSB_MASK) != Constants.RANDOM_MSB_MASK) {
//			return new NewULID(msb + 1, 0);
//		}
//
//		// TODO: Log overflow?
//		if (strict) {
//			throw new IllegalStateException("Strict incrmenting does not permit overflowing randomness region");
//		}
//
//		return new NewULID(msb & Constants.TIMESTAMP_MSB_MASK, 0);
//	}

	@Override
	public int hashCode() {
		long hilo = mostSignificantBits ^ leastSignificantBits;
		return ((int) (hilo >> 32)) ^ (int) hilo;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		ULID value = (ULID) o;

		return mostSignificantBits == value.mostSignificantBits && leastSignificantBits == value.leastSignificantBits;
	}

	@Override
	public int compareTo(ULID val) {
		// The ordering is intentionally set up so that the ULIDs
		// can simply be numerically compared as two numbers
		return (this.mostSignificantBits < val.mostSignificantBits ? -1
				: (this.mostSignificantBits > val.mostSignificantBits ? 1
						: (this.leastSignificantBits < val.leastSignificantBits ? -1
								: (this.leastSignificantBits > val.leastSignificantBits ? 1 : 0))));
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder(26);
		internalEncodeULID(buffer, this.mostSignificantBits, this.leastSignificantBits);
		return buffer.toString();
	}

	public void appendULID(StringBuilder stringBuilder)
	{
		Objects.requireNonNull(stringBuilder, "stringBuilder must not be null!");
		internalEncodeULID(stringBuilder, this.mostSignificantBits, this.leastSignificantBits);
		
	}
	
	public static ULID parseULIDPath(String ulidPath, String separator) {
		return parseULID(ulidPath.replaceAll(separator, ""));
	}

	public static ULID parseULID(String ulidString) {
		Objects.requireNonNull(ulidString, "ulidString must not be null!");
		if (ulidString.length() != 26) {
			throw new IllegalArgumentException("ulidString must be exactly 26 chars long.");
		}

		CharSequence timeString = ulidString.subSequence(0, 10);
		long timestamp = internalDecodeWord(timeString);
		checkTimestamp(timestamp, false);

//		long timestamp = internalCrockfordParseTime(timeString);
//		String randomMsb = ulidString.substring(10, 14);
//		String randomLsb = ulidString.substring(13);
//		long most = internalCrockfordParseRandomMsb(timestamp, randomMsb);
//		long least = internalCrockfordParseRandomLsb(randomLsb);

		CharSequence hiPartString = ulidString.subSequence(10, 18);
		CharSequence loPartString = ulidString.subSequence(18, 26);
		long hiPart40 = internalDecodeWord(hiPartString);
		long loPart40 = internalDecodeWord(loPartString);

		return fromTimeHi40Lo40(timestamp, hiPart40, loPart40);
	}

	public static ULID fromBytes(byte[] data) {
		Objects.requireNonNull(data, "data must not be null!");
		if (data.length != 16) {
			throw new IllegalArgumentException("data must be 16 bytes in length!");
		}
		long mostSignificantBits = 0;
		long leastSignificantBits = 0;
		for (int i = 0; i < 8; i++) {
			mostSignificantBits = (mostSignificantBits << 8) | (data[i] & 0xff);
		}
		for (int i = 8; i < 16; i++) {
			leastSignificantBits = (leastSignificantBits << 8) | (data[i] & 0xff);
		}
		return new ULID(mostSignificantBits, leastSignificantBits);
	}

	public static ULID fromTimeHi16Lo64(long timestamp, int hiRandom16, long loRandom64) {
		checkTimestamp(timestamp);
		if (hiRandom16 != (hiRandom16 & Constants.RANDOM_16_MASK)) {
			throw new IllegalArgumentException("hiRandom16 overflows its least significant 16 bits");
		}

		final long mostSignificantBits =
			(hiRandom16 & Constants.RANDOM_16_MASK) | (timestamp << Constants.ULID_TO_CLOCK_SHIFT);
		final long leastSignificantBits = loRandom64;

		return new ULID(mostSignificantBits, leastSignificantBits);
	}

	public static ULID fromTimeHi40Lo40(long timestamp, long hiRandom40, long loRandom40) {
		checkTimestamp(timestamp);
		if (hiRandom40 != (hiRandom40 & Constants.RANDOM_40_MASK)) {
			throw new IllegalArgumentException(
				String.format("hiRandom40 overflows its least significant 16 bits at %d", hiRandom40)
			);
		}
		if (loRandom40 != (loRandom40 & Constants.RANDOM_40_MASK)) {
			throw new IllegalArgumentException(
				String.format("loRandom40 overflows its least significant 16 bits at %d", loRandom40)
			);
		}

		long mostSignificantBits =
			(timestamp << Constants.ULID_TO_CLOCK_SHIFT) | (hiRandom40 >>> Constants.HI_RANDOM_40_MSB_SHIFT);
	    long leastSignificantBits =
	    	loRandom40 | (hiRandom40 << Constants.HI_RANDOM_40_LSB_SHIFT);

		return new ULID(mostSignificantBits, leastSignificantBits);
	}
	
	public static ULID fromHi64Lo64(long mostSignificantBits, long leastSignificantBits) {
		return new ULID(mostSignificantBits, leastSignificantBits);
	}

	/*
	 * http://crockford.com/wrmg/base32.html
	 */
	static void internalEncodeTimeHi40Lo40(StringBuilder builder, long timestamp, long randomMsb40, long randomLsb40) {
		internalEncodeWord(builder, timestamp, 10);
		internalEncodeWord(builder, randomMsb40, 8);
		internalEncodeWord(builder, randomLsb40, 8);
	}
	
	static void internalEncodeULID(StringBuilder builder, long most64, long least64) {
		builder.append(
			Constants.SHORT_ENCODING_CHARS[
			    (int) ((most64 >>> Constants.ULID_TO_TIME_MSB_SHIFT) & Constants.MSB_TIME_CHAR_MASK)
			]
		);
		int nextShift = Constants.ULID_TO_TIME_MSB_SHIFT - Constants.BASE32_CHAR_MASK_BITS;
		for (; nextShift >= Constants.MSB_BOUNDARY_MASK_BITS; nextShift = nextShift - Constants.BASE32_CHAR_MASK_BITS) {
    		builder.append(
    			Constants.ENCODING_CHARS[
    			    (int) ((most64 >>> nextShift) & Constants.BASE32_CHAR_MASK)
    			]
    		);
		}
		builder.append(
			Constants.BOUNDARY_ENCODING_CHARS[
	    	    (int) (most64 & Constants.MSB_BOUNDARY_MASK)
			][
                (int) ((least64 >>> Constants.ULID_TO_LSB_BOUNDARY_SHIFT) & Constants.LSB_BOUNDARY_MASK)
			]
		);
		nextShift = Constants.ULID_TO_LSB_BOUNDARY_SHIFT - Constants.BASE32_CHAR_MASK_BITS;
		for (; nextShift > 0; nextShift = nextShift - Constants.BASE32_CHAR_MASK_BITS) {
    		builder.append(
    			Constants.ENCODING_CHARS[
    			    (int) ((least64 >>> nextShift) & Constants.BASE32_CHAR_MASK)
    			]
    		);
		}
		builder.append(
			Constants.ENCODING_CHARS[
			    (int) (least64 & Constants.BASE32_CHAR_MASK)
			]
		);
	}

	private static void internalEncodeWord(StringBuilder builder, long value, int count) {
		int nextShift = (count - 1) * Constants.BASE32_CHAR_MASK_BITS;
		for (; nextShift > 0; nextShift = nextShift - Constants.BASE32_CHAR_MASK_BITS) {
			final int index = (int) ((value >>> nextShift) & Constants.BASE32_CHAR_MASK);
			builder.append(Constants.ENCODING_CHARS[index]);
		}
		builder.append(Constants.ENCODING_CHARS[(int) value & Constants.BASE32_CHAR_MASK]);
	}


	private static long internalDecodeWord(CharSequence input) {
		Objects.requireNonNull(input, "input must not be null!");
		int length = input.length();
		if (length > 12) {
			throw new IllegalArgumentException("input length must not exceed 12 but was " + length + "!");
		}

		return input.codePoints()
			.map(ULID::decodeChar)
			.asLongStream()
			.reduce(0L, (long agg, long next) -> {
			    return ((agg << Constants.BASE32_CHAR_MASK_BITS) | next);
			}
		);
	}

	private static byte decodeChar(int codePoint) {
		byte value = -1;
		if (codePoint < Constants.DECODING_CHARS.length) {
			value = Constants.DECODING_CHARS[(char) codePoint];
		}
		if (value < 0) {
			throw new IllegalArgumentException(
				"Illegal character codepoint'" + codePoint + "'!"
			);
		}
		return value;
	}

	/*
	 * http://crockford.com/wrmg/base32.html
	 */

	private static void checkTimestamp(long timestamp) {
		checkTimestamp(timestamp, true);
	}

	private static void checkTimestamp(long timestamp, boolean fromClock) {
		if ((timestamp & Constants.TIMESTAMP_OVERFLOW_MASK) != 0) {
			if (fromClock) {
				throw new IllegalArgumentException(
						"ULID does not support timestamps after +10889-08-02T05:31:50.655Z!");
			} else {
				throw new IllegalArgumentException("ulidString must not exceed '7ZZZZZZZZZZZZZZZZZZZZZZZZZ'!");
			}
		}
	}
}
