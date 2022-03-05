package name.jchein.ptflibs.identity.ulid;

@FunctionalInterface
public interface EqualSplitBitsProvider {
	void accept(long hi40Bits, long lo40Bits);
}