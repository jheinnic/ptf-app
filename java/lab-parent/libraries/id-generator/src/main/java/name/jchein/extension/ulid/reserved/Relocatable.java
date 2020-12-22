package name.jchein.extension.ulid.reserved;

public interface Relocatable {
    void updateLocation(long location, int bitsUsed);
}
