package name.jchein.ptflibs.identity.ulid;

public enum BehaviorOnExhaustion {
    THROW_EXCEPTION,
    SPIN_ON_CLOCK,
    SLEEP_ONE_TICK,
    RETURN_NULL,
    SKEW_CLOCK_FORWARD,
    RANDOMIZE_LOCATION
}
