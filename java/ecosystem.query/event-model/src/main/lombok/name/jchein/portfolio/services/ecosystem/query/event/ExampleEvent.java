package name.jchein.portfolio.services.ecosystem.query.event;


import io.eventuate.Event;
import io.eventuate.EventEntity;


@EventEntity(entity="name.jchein.portfolio.services.ecosystem.query.domain.Example")
public interface ExampleEvent extends Event
{
}
