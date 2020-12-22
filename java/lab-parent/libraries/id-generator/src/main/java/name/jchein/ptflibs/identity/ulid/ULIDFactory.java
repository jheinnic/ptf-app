package name.jchein.ptflibs.identity.ulid;

public interface ULIDFactory {
    String nextULIDString();
    
    void appendULIDString(StringBuilder buffer);
    
//    String nextULIDPath(char separator);
    
//    void appendULIDPath(StringBuilder buffer, char separator);
    
    ULID nextULID();
    
    ULID altNextULID();
}
