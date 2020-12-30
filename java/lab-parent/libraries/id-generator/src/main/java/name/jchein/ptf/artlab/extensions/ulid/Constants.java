package name.jchein.ptf.artlab.extensions.ulid;

public final class Constants {
	private Constants() { }
	
	static final long LOCATION_HI_16_MASK = 0xFFFF_0000_0000_0000L;
	static final long LOCATION_HI_40_MASK = 0xFFFF_FFFF_FF00_0000L;
	static final long LOCATION_LO_40_MASK = 0x0000_0000_00FF_FFFFL;
	static final long LOCATION_LO_64_MASK = 0x0000_FFFF_FFFF_FFFFL;
	
//	static final long BASE_RANDOM_HI_16_MASK = 0x0000_0000_0000_0000L;
	static final long BASE_RANDOM_HI_40_MASK = 0xFFFF_FF00_0000_0000L;
	static final long BASE_RANDOM_LO_40_MASK = 0x0000_00FF_FFFF_FFFFL;
	static final long BASE_RANDOM_LO_64_MASK = 0xFFFF_FFFF_FFFF_FFFFL;
	

}
