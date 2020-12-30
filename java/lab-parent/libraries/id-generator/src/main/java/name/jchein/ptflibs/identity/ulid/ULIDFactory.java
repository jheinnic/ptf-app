package name.jchein.ptflibs.identity.ulid;

import java.util.Objects;

public interface ULIDFactory {
//	static ULIDFactory configure(Consumer<ULIDFactoryConfigBuilder> director) {
//		
//	}
	public default String nextULIDString()
	{
		final StringBuilder builder = new StringBuilder(26);
		this.appendULID(builder);
		return builder.toString();
	}
    
	public default void appendULID(StringBuilder builder)
	{
		Objects.requireNonNull(builder, "stringBuilder must not be null!");
		final ULID nextULID = nextULID();
		ULID.encodeHi64Lo64(
			builder,
			nextULID.getMostSignificantBits(),
			nextULID.getLeastSignificantBits()
		);
	}

//    String nextULIDPath(char separator);
    
//    void appendULIDPath(StringBuilder buffer, char separator);
    
    ULID nextULID();
    
    ULID altNextULID();
}
