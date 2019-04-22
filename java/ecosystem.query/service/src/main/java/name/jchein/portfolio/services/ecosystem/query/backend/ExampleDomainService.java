package name.jchein.portfolio.services.ecosystem.query.backend;


import io.eventuate.EntityWithIdAndVersion;
import io.eventuate.sync.AggregateRepository;
import name.jchein.common.validation.constraints.UUIDString;
import name.jchein.portfolio.common.es.eventuate.IUUIDExtension;

import name.jchein.portfolio.services.ecosystem.query.domain.CreateExample;
import name.jchein.portfolio.services.ecosystem.query.domain.Example;
import name.jchein.portfolio.services.ecosystem.query.domain.ExampleCommand;


public class ExampleDomainService
implements IExampleDomainService
{
   private final AggregateRepository<Example, ExampleCommand> exampleRepository;
   private final IUUIDExtension uuidExtension;


   public ExampleDomainService( AggregateRepository<Example, ExampleCommand> exampleRepository, IUUIDExtension uuidExtension )
   {
      this.exampleRepository = exampleRepository;
      this.uuidExtension = uuidExtension;
   }


   @Override
   public EntityWithIdAndVersion<Example> createExample(
      @UUIDString String exampleUuid,
      @UUIDString String pilotUuid,
      String firstName,
      String middleName,
      String lastName)
   {
      return this.exampleRepository.save(CreateExample.build((bldr) -> {
         bldr.pilotUuid(pilotUuid)
             .firstName(firstName)
             .middleName(middleName)
             .lastName(lastName);
      }), this.uuidExtension.toSaveOptions(exampleUuid));
   }
}
