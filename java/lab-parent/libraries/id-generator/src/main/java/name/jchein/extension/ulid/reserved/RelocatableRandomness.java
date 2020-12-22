package name.jchein.extension.ulid.reserved;

import name.jchein.ptflibs.identity.ulid.ULIDRandomBitsStrategy;

public class RelocatableRandomness implements ULIDRandomBitsStrategy, Relocatable {
	private final ULIDRandomBitsStrategy baseRandomness;
	private int randomHi16;
	private long randomHi40;
	private long randomLo40;
	private long randomLo64;

	public RelocatableRandomness(ULIDRandomBitsStrategy baseRandomness) {
		this.baseRandomness = baseRandomness;
		this.randomHi40 = baseRandomness.getRandomHi40();
		this.randomLo40 = baseRandomness.getRandomLo40();
		this.randomHi16 = (int) (this.randomHi40 >>> 24);
		this.randomLo64 = (this.randomHi40 << 40) | this.randomLo64;
	}

	public static RelocatableRandomness initStrategy(
		ULIDRandomBitsStrategy baseRandomness, long firstLocation, int bitsUsed
	) {
		RelocatableRandomness retVal = new RelocatableRandomness(baseRandomness);
		retVal.updateLocation(firstLocation, bitsUsed);
		return retVal;
	}
		
		
	@Override
	public void updateLocation(long location, int bitsUsed) {
		int offsetShift = 64 - bitsUsed;
		
		// For location, offsetShift describes the left shift required to 
		// place the most significant digit of the retained region on the 
		// far-left bit of a normalized word.  Standard masks can then be
		// applied to break out the regions, after which the low order 
		// words must shift another 16 bits to the left to account for 
		// 16 right-most bits of our 80-bit address space we could not
		// accommodate contiguously.
		long localHi64 = location << offsetShift;
		this.randomLo64 = (localHi64 & Constants.LOCATION_LO_64_MASK) << 16;
		this.randomLo40 = (localHi64 & Constants.LOCATION_LO_40_MASK) << 16;
		this.randomHi40 = localHi64 & Constants.LOCATION_HI_40_MASK;
		this.randomHi16 = (int) ((localHi64 & Constants.LOCATION_HI_16_MASK) >> 32);

		// For the initial random filler for our monotonic counter bits
		// to right of reserved location address bits, offsetShift counts
		// additional masking bits added to baseline of 16 right-most
		// masking bits used to select what portion of randomly generated
		// pad is kept.
		long rndMaskMask = 0xFFFFL;
		for (int ii = 0; ii < offsetShift; ii++) {
			rndMaskMask = (rndMaskMask << 1) | 0x1L;
		}
		
		// masking mask has 16 + offsetShift right-most bits.  Use it to
		// prune each of the full-region masks that would be used to select
		// the entire low 64 bit space if they were not pruned.
		long rndLo64Mask = rndMaskMask & Constants.BASE_RANDOM_LO_64_MASK;
		long rndLo40Mask = rndMaskMask & Constants.BASE_RANDOM_LO_40_MASK;
		long rndHi40Mask = rndMaskMask & Constants.BASE_RANDOM_HI_40_MASK;

		// Generate the padding and apply the pruned selection masks to 
		// get the appropriate low-order generated padding for our
		// non-reserved part of the 80 bit address space used for 
		// monotonically increasing conflict resolution.
		// The upper MSB word must be shifted an additional 16 bits to
		// the right to account for 16 left-most bits of our 80-bit
		// address space we could not accommodate contiguously.
		long baseRndLo64 = this.baseRandomness.getRandomLo64();
		this.randomLo64 = this.randomLo64 | (baseRndLo64 & rndLo64Mask);
		this.randomLo40 = this.randomLo40 | (baseRndLo64 & rndLo40Mask);
		this.randomHi40 = this.randomHi40 | ((baseRndLo64 & rndHi40Mask) >> 16);
		
		// The upper significant bit words are currently aligned with
		// their most significant bit at the highest index.  We need 
		// them to align with their least significant bit at index=0,
		// which they cannot possibly be since neither is a full 64 bit
		// word.  The low order words are indeed already correctly aligned.
		// Remember that randomHi16 is a 32-bit int and not a 64-bit long!
		this.randomHi16 = this.randomHi16 >>> 16;
		this.randomHi40 = this.randomHi40 >>> 24;
	}

	@Override
	public long getRandomHi40() {
		return this.randomHi40;
	}

	@Override
	public int getRandomHi16() {
		return this.randomHi16;
	}

	@Override
	public long getRandomLo40() {
		return this.randomLo40;
	}

	@Override
	public long getRandomLo64() {
		return this.randomLo64;
	}

}
