package name.jchein.ptflibs.identity.ulid;

import java.math.BigInteger;

public final class Constants {
	private Constants() { }
	
	static final char[] ENCODING_CHARS = {
			'0','1','2','3','4','5','6','7','8','9',
			'A','B','C','D','E','F','G','H','J','K',
			'M','N','P','Q','R','S','T','V','W','X',
			'Y','Z',
	};
	static final char[] SHORT_ENCODING_CHARS = {
		'0','1','2','3','4','5','6','7'
	};
	static final char[][] BOUNDARY_ENCODING_CHARS = {
		{ '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F' },
		{ 'G','H','J','K','M','N','P','Q','R','S','T','V','W','X','Y','Z' }
	};

	static final byte[] DECODING_CHARS = {
			// 0
			-1, -1, -1, -1, -1, -1, -1, -1,
			// 8
			-1, -1, -1, -1, -1, -1, -1, -1,
			// 16
			-1, -1, -1, -1, -1, -1, -1, -1,
			// 24
			-1, -1, -1, -1, -1, -1, -1, -1,
			// 32
			-1, -1, -1, -1, -1, -1, -1, -1,
			// 40
			-1, -1, -1, -1, -1, -1, -1, -1,
			// 48
			0, 1, 2, 3, 4, 5, 6, 7,
			// 56
			8, 9, -1, -1, -1, -1, -1, -1,
			// 64
			-1, 10, 11, 12, 13, 14, 15, 16,
			// 72
			17, 1, 18, 19, 1, 20, 21, 0,
			// 80
			22, 23, 24, 25, 26, -1, 27, 28,
			// 88
			29, 30, 31, -1, -1, -1, -1, -1,
			// 96
			-1, 10, 11, 12, 13, 14, 15, 16,
			// 104
			17, 1, 18, 19, 1, 20, 21, 0,
			// 112
			22, 23, 24, 25, 26, -1, 27, 28,
			// 120
			29, 30, 31, -1, -1, -1, -1, -1,
			// 128
	};

	static final byte[] MSB_BOUNDARY_DECODING_CHARS = {
			// 0
			-1, -1, -1, -1, -1, -1, -1, -1,
			// 8
			-1, -1, -1, -1, -1, -1, -1, -1,
			// 16
			-1, -1, -1, -1, -1, -1, -1, -1,
			// 24
			-1, -1, -1, -1, -1, -1, -1, -1,
			// 32
			-1, -1, -1, -1, -1, -1, -1, -1,
			// 40
			-1, -1, -1, -1, -1, -1, -1, -1,
			// 48
			0, 0, 0, 0, 0, 0, 0, 0,
			// 56
			0, 0, -1, -1, -1, -1, -1, -1,
			// 64
			-1, 0, 0, 0, 0, 0, 0, 1,
			// 72
			1, 0, 1, 1, 0, 1, 1, 0,
			// 80
			1, 1, 1, 1, 1, -1, 1, 1,
			// 88
			1, 1, 1, -1, -1, -1, -1, -1,
			// 96
			-1, 0, 0, 0, 0, 0, 0, 1,
			// 104
			1, 0, 1, 1, 0, 1, 1, 0,
			// 112
			1, 1, 1, 1, 1, -1, 1, 1,
			// 120
			1, 1, 1, -1, -1, -1, -1, -1,
			// 128
	};

	static final byte[] LSB_BOUNDARY_DECODING_CHARS = {
			// 0
			-1, -1, -1, -1, -1, -1, -1, -1,
			// 8
			-1, -1, -1, -1, -1, -1, -1, -1,
			// 16
			-1, -1, -1, -1, -1, -1, -1, -1,
			// 24
			-1, -1, -1, -1, -1, -1, -1, -1,
			// 32
			-1, -1, -1, -1, -1, -1, -1, -1,
			// 40
			-1, -1, -1, -1, -1, -1, -1, -1,
			// 48
			0, 0, 0, 0, 0, 0, 0, 0,
			// 56
			8, 9, -1, -1, -1, -1, -1, -1,
			// 64
			-1, 10, 11, 12, 13, 14, 15, 0,
			// 72
			1, 1, 2, 3, 1, 4, 5, 0,
			// 80
			6, 7, 8, 9, 10, -1, 11, 12,
			// 88
			13, 14, 15, -1, -1, -1, -1, -1,
			// 96
			-1, 10, 11, 12, 13, 14, 15, 0,
			// 104
			1, 1, 2, 3, 1, 4, 5, 0,
			// 112
			6, 7, 8, 9, 10, -1, 11, 12,
			// 120
			13, 14, 15, -1, -1, -1, -1, -1,
			// 128
	};

	static final int BASE32_CHAR_MASK = 0x1F;
	static final int MSB_TIME_CHAR_MASK = 0x7;
	static final int MSB_BOUNDARY_MASK = 0x1;
	static final int LSB_BOUNDARY_MASK = 0xF;

	static final long FULL_WORD_MASK = 0xFFFF_FFFF_FFFF_FFFFL;
	static final long TIMESTAMP_OVERFLOW_MASK = 0xFFFF_0000_0000_0000L;
	static final long TIMESTAMP_MSB_MASK = 0xFFFF_FFFF_FFFF_0000L;
	public static final long HI_RANDOM_16_MSB_MASK = 0x0000_0000_0000_FFFFL;
	public static final long HI_RANDOM_24_LSB_MASK = 0xFFFF_FF00_0000_0000L;
	public static final long LO_RANDOM_40_LSB_MASK = 0x0000_00FF_FFFF_FFFFL;
	public static final long RANDOM_40_MASK = 0x0000_00FF_FFFF_FFFFL;
	public static final int RANDOM_24_MASK = 0x00FF_FFFF;
	public static final int RANDOM_16_MASK = 0x0000_FFFF;
	public static final long SINGLE_BYTE_MASK = 0xFFL;

	static final int BASE32_CHAR_MASK_BITS = 5;
//	static final int MSB_TIME_CHAR_MASK_BITS = 3;
	static final int MSB_BOUNDARY_MASK_BITS = 1;
//	static final int LSB_BOUNDARY_MASK_BITS = 4;
	static final int RANDOM_40_MASK_BITS = 40;
	static final int BYTE_BITS = 8;

	static final int CLOCK_TO_TIME_MSB_SHIFT = 50;
	static final int ULID_TO_CLOCK_SHIFT = 16;
	static final int ULID_TO_TIME_MSB_SHIFT = 61;

	static final int ULID_TO_LSB_BOUNDARY_SHIFT = 60;
	static final int HI_RANDOM_40_MSB_SHIFT = 24;
	static final int HI_RANDOM_40_LSB_SHIFT = 40;
	
	 
	// Config root
	static final String CONFIG_PREFIX = "jchptf.idgen";
	
	static final int VARIANT_BITS_LEN = 3;
	static final int VARIANT_BITS_MASK = 0x7;
	
	static final int MIN_EPOCH_FIELD_PRIME_BITS = 6;
	static final int MAX_EPOCH_FIELD_PRIME_BITS = 10;
	
	static final int SERIES_BITS_SHIFT = 3;
//	static final int SERIES_BITS_STEP = 8;

	static final BigInteger LO_40_MASK =
		BigInteger.valueOf(0x0000_00FF_FFFF_FFFFL);
	static final BigInteger LO_64_MASK = 
		BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE);
	
	static final long LOCATION_HI_16_MASK = 0xFFFF_0000_0000_0000L;
	static final long LOCATION_HI_40_MASK = 0xFFFF_FFFF_FF00_0000L;
	static final long LOCATION_LO_40_MASK = 0x0000_0000_00FF_FFFFL;
	static final long LOCATION_LO_64_MASK = 0x0000_FFFF_FFFF_FFFFL;
	
	static final long BASE_RANDOM_HI_40_MASK = 0xFFFF_FF00_0000_0000L;
	static final long BASE_RANDOM_LO_40_MASK = 0x0000_00FF_FFFF_FFFFL;
	static final long BASE_RANDOM_LO_64_MASK = 0xFFFF_FFFF_FFFF_FFFFL;
}
