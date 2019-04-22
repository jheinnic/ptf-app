package name.jchein.portfolio.services.paint.gateway.domain;


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
import name.jchein.portfolio.services.paint.gateway.event.ExampleCreated;


@ValidateOnExecution(type = ExecutableType.NON_GETTER_METHODS)
public class Example
extends ReflectiveMutableCommandProcessingAggregate<Example, ExampleCommand>
{
	// Convenience value for no-op commands
	// private static final List<Event> EMPTY_LIST =
	// ImmutableList.<Event> builder().build();

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
		return EventUtil.events(ExampleCreated.build((it) -> {
			it.firstName(cmd.getFirstName())
				.middleName(cmd.getMiddleName())
				.lastName(cmd.getLastName())
				.pilotUuid(cmd.getPilotUuid());
		}));
	}
}
