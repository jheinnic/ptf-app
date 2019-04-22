package name.jchein.portfolio.services.paint.gateway.event;


import java.util.function.Consumer;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

import name.jchein.common.validation.constraints.UUIDString;


@Value
@AllArgsConstructor(access=AccessLevel.PUBLIC)
@NoArgsConstructor(access=AccessLevel.PUBLIC, force=true)
@Builder(toBuilder=true)
public class ExampleCreated
implements ExampleEvent
{
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
   
   public static ExampleCreated build(Consumer<ExampleCreatedBuilder> director) {
      final ExampleCreatedBuilder builder = ExampleCreated.builder();
      director.accept(builder);
      return builder.build();
   }
   
   public ExampleCreated copy(Consumer<ExampleCreatedBuilder> director) {
      final ExampleCreatedBuilder builder = this.toBuilder();
      director.accept(builder);
      return builder.build();
   }
}
