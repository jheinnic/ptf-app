package name.jchein.portfolio.services.ecosystem.action.domain;


import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;

import io.eventuate.EventHandlerContext;
import io.eventuate.EventHandlerMethod;
import io.eventuate.EventSubscriber;
import lombok.extern.slf4j.Slf4j;

import name.jchein.common.identity.IUUIDGenerator;

import name.jchein.portfolio.services.ecosystem.action.event.ExampleCreated;


@Slf4j
@EventSubscriber(id = "newExampleCreation")
public class NewExampleCreationHandler
{
   private IUUIDGenerator uuidGenerator;


   public NewExampleCreationHandler( @Autowired IUUIDGenerator uuidGenerator )
   {
      this.uuidGenerator = uuidGenerator;
   }


   @EventHandlerMethod
   public CompletableFuture<?> enrollNewEmployee(EventHandlerContext<ExampleCreated> evtCxt)
   {
      final ExampleCreated evt = evtCxt.getEvent();
      final String employeeId = evtCxt.getEntityId();
      final String pilotId = evt.getPilotUuid();
      final String newExampleId = this.uuidGenerator.nextIdentifier().toString();

      log.warn(
         "Handling creation of example {} for pilot {} by creating example {} for pilot {}",
         employeeId,
         pilotId,
         newExampleId,
         employeeId);
      return evtCxt.save(Example.class, CreateExample.build((builder) -> {
         builder.pilotUuid(employeeId)
            .firstName(evt.getFirstName())
            .middleName(evt.getMiddleName())
            .lastName(evt.getLastName());
      }), Optional.of(newExampleId));
   }
}
