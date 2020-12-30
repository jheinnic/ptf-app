package name.jchein.ptf.artlab.extensions.ulid;

public interface Relocatable {
    void updateLocation(long location, int bitsUsed);
}
