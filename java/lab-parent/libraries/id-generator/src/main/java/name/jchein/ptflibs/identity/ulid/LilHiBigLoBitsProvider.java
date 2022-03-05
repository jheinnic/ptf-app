package name.jchein.ptflibs.identity.ulid;

@FunctionalInterface
public interface LilHiBigLoBitsProvider {
	void accept(int hi16Bits, long lo64Bits);
}