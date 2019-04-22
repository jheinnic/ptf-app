package name.jchein.portfolio.services.ecosystem.action.event;


import io.eventuate.Event;
import io.eventuate.EventEntity;


@EventEntity(entity="name.jchein.portfolio.services.ecosystem.action.domain.Example")
public interface ExampleEvent extends Event
{
}
