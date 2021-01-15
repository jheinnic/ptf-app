package name.jchein.ptflibs.math.field;

public class FindSome {
    public static void main(String[] args) {
    	final PrimePowerGroup bigPpf =
    		PrimePowerGroup.findGroupByCharacteristic(40, 8, 14);
    	final LongGroupGeneratorSpliterator order =
    		bigPpf.findBasePrimeGenerator();
    	System.out.println(
    		String.format("<%d> <%d> <%d> <%d> <%d>",
    			order.getP(), order.getPN(), order.getG(), order.getExponent(), order.getRemaining()));
    	long[] results = new long[5];
    	order.tryAdvance( (long next) -> {
    		results[0] = next;
    	});
    	order.tryAdvance( (long next) -> {
    		results[1] = next;
    	});
    	order.tryAdvance( (long next) -> {
    		results[2] = next;
    	});
    	order.tryAdvance( (long next) -> {
    		results[3] = next;
    	});
    	order.tryAdvance( (long next) -> {
    		results[4] = next;
    	});
    	System.out.println(
    		String.format("<%d> <%d> <%d> <%d> <%d>",
    			results[0], results[1], results[2], 
    			results[3], results[4]
    		)
    	);
    }
}
