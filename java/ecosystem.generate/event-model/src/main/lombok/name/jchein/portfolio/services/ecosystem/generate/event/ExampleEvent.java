package name.jchein.portfolio.services.ecosystem.generate.event;


import io.eventuate.Event;
import io.eventuate.EventEntity;


@EventEntity(entity="name.jchein.portfolio.services.ecosystem.generate.domain.Example")
public interface ExampleEvent extends Event
{
}
