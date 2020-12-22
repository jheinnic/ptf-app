package name.jchein.ptflibs.identity.ulid;

public interface ULIDRandomBitsStrategy {
//    public void nextBytes(@Size(min=10, max=10) byte[] bytes);
//	public byte[] nextBytes();
	
	/**
	 * Only the least significant 40 bits of returned long will be used to supply 
	 * 40 most significant bits of a ULID's random region.
	 * @return
	 */
	public long getRandomHi40();
	
	/**
	 * Only the least significant 16 bits of returned int will be used to supply 
	 * 16 least significant bits of a ULID's random region.
	 * @return
	 */
	public int getRandomHi16();
	
	/**
	 * Only the least significant 40 bits of returned long will be used to supply 
	 * 40 most significant bits of a ULID's random region.
	 * @return
	 */
	public long getRandomLo40();
	
	/**
	 * All 64 bits of returned long will be used to supply 64 least significant
	 * bits of a ULID's random region.
	 * @return
	 */
	public long getRandomLo64();
}
