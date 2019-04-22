package name.jchein.portfolio.services.paint.gateway.event;


import io.eventuate.Event;
import io.eventuate.EventEntity;


@EventEntity(entity="name.jchein.portfolio.services.paint.gateway.domain.Example")
public interface ExampleEvent extends Event
{
}
