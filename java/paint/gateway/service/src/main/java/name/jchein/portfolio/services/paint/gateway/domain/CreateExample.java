package name.jchein.portfolio.services.paint.gateway.domain;

import java.util.function.Consumer;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.ScriptAssert;

import lombok.Builder;
import lombok.Value;
import name.jchein.common.validation.constraints.UUIDString;

@Value
@Builder(toBuilder=true)
@ScriptAssert.List({
})
public class CreateExample implements ExampleCommand {
    @NotNull
    @UUIDString
    String pilotUuid;
    
    @NotNull
    @NotBlank
    String firstName;

    @NotBlank
    String middleName;

    @NotNull
    @NotBlank
    String lastName;
   
    public static CreateExample build(Consumer<CreateExampleBuilder> director) {
        final CreateExampleBuilder bldr = CreateExample.builder();
        director.accept(bldr);
        return bldr.build();
    }
    
    public CreateExample copy(Consumer<CreateExampleBuilder> director) {
        final CreateExampleBuilder bldr = this.toBuilder();
        director.accept(bldr);
        return bldr.build();
    }
}
