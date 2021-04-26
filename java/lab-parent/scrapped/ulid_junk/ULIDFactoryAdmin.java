package name.jchein.ptflibs.identity.ulid.junk;

import java.util.function.Consumer;

public interface ULIDFactoryAdmin {
    void reconfigure(Consumer<ULIDFactoryConfigBuilder> director);
}
