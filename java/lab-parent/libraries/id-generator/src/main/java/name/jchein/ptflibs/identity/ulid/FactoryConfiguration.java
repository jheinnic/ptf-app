package name.jchein.ptflibs.identity.ulid;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FactoryConfiguration {
	/**
	 * If true, the previous ULID is tracked to prevent the clock
	 * from moving backwards, and instead of polling the randomizer,
	 * the tail end of the random location bit region is increased.
	 * 
	 * If false, no tracking is done, and therefore no other 
	 * configuration properties regarding the monotonic bit range
	 * are applied.  When state is not tracked, the location
	 * region is randomized on every request, which only provides
	 * a probability-based assurance of uniqueness.
	 */
    boolean trackPrevious;
    
    /**
     * If trackPrevious is true, determines how many rightmost
     * bits of location region may be monotonically increased 
     * without re-rolling location whenever the clock fails to
     * increase since previous ID was generated.
     */
    int totalMonotonicBitRange;
    
    /**
     * If trackPrevious is true then allowMonitonicTrap determines
     * whether the monotonic region may wrap to zero and count back
     * up to its initial value before exhausting (true) or is
     * exhausted as some a it reaches all 1's (false)
	 */
    boolean allowMonotonicWrap;
    
    /**
     * If trackPrvious is true and allowMonotonicWrap is false, this
     * determines how many bits on the left-hand size of 
     * monotonicBitRange should always be initially zeroed out 
     * to ensure some minimum number of increments are always
     * supported.
     * 
     * When used, this value must by definition be no greater than
     * totalMonotonicBitRange.
     */
    int initMonotonicZeroes;
    
    /**
     * Determines behavior on exhausting the monotonic region
     * provided for clock conflict resolution
     */
    BehaviorOnExhaustion exhaustionBehavior;
}
