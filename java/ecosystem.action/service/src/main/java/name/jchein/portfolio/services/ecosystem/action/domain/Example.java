package name.jchein.portfolio.services.ecosystem.action.domain;


import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;

import org.hibernate.validator.constraints.NotBlank;

import io.eventuate.Event;
import io.eventuate.EventUtil;
import io.eventuate.ReflectiveMutableCommandProcessingAggregate;

import name.jchein.common.validation.constraints.UUIDString;

import name.jchein.portfolio.services.ecosystem.action.event.ExampleCreated;


@ValidateOnExecution(type = ExecutableType.NON_GETTER_METHODS)
public class Example
extends ReflectiveMutableCommandProcessingAggregate<Example, ExampleCommand>
{
   @NotNull
   @UUIDString
   String pilotUuid;

   @NotNull
   @NotBlank
   String firstName;

   @NotNull
   String middleName;

   @NotNull
   @NotBlank
   String lastName;


   public void apply(@Valid ExampleCreated evt)
   {
      this.pilotUuid = evt.getPilotUuid();
      this.firstName = evt.getFirstName();
      this.middleName = evt.getMiddleName();
      this.lastName = evt.getLastName();
   }


   public List<Event> process(@Valid CreateExample cmd)
   {
      return EventUtil.events(ExampleCreated.build((bld) -> {
         bld.firstName(cmd.getFirstName())
            .middleName(cmd.getMiddleName())
            .lastName(cmd.getLastName())
            .pilotUuid(cmd.getPilotUuid());
      }));
   }
}
