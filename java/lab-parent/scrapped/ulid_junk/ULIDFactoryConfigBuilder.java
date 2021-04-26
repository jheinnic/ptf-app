package name.jchein.ptflibs.identity.ulid.junk;

import java.time.Clock;
import java.time.Duration;

import name.jchein.ptflibs.identity.ulid.ULIDRandomBitsStrategy;

public interface ULIDFactoryConfigBuilder {
    ULIDFactoryConfigBuilder setClock(Clock clock, Duration tickDuration, int shortTermTicks);
    
    ULIDFactoryConfigBuilder setRandom(ULIDRandomBitsStrategy random);
    
    ULIDFactoryConfigBuilder setOnExhaustion( BehaviorOnExhaustion onExhaustion);
    
    ULIDFactoryConfigBuilder setOnShortTermExhaustion( BehaviorOnExhaustion onShortExhaustion);
    
    ULIDFactoryConfigBuilder setWrapMonoMode(int monoBitCount);

    ULIDFactoryConfigBuilder setStrictMonoMode(int monoBitCount, int monoZeroedBits);
    
    ULIDFactoryConfigBuilder disableClockTracking();
}
