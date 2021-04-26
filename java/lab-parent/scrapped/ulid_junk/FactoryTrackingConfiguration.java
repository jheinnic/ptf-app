package name.jchein.ptflibs.identity.ulid.junk;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
/**
 * If provided, then factory tracking is enabled and behaves as 
 * described by given instance's values.  If no instance is provided,
 * tracking is disabled and every ULID request requires a call to
 * its ULIDRandomBitsStrategy.
 * 
 * @author jheinnic
 *
 */
public class FactoryTrackingConfiguration {
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
     * determines how many of the most significant bits from  
     * monotonicBitRange will always be initially masked to 0 in order 
     * to ensure some minimum number of increments are always available,
     * even if the remaining least significant bits in range all randomly 
     * got initialized as 1's. 
     * 
     * (((2^n) - 1) - ((2^(n-z)) - 1)) 
     * 
     * where n is the total number of monotonic range bits and z is the
     * value for this paramter, the number of most-significant bits always
     * set to 0 initially.
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
