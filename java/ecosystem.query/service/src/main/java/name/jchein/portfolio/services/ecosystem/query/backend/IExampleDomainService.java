package name.jchein.portfolio.services.ecosystem.query.backend;


import io.eventuate.EntityWithIdAndVersion;
import name.jchein.portfolio.services.ecosystem.query.domain.Example;


public interface IExampleDomainService
{
   EntityWithIdAndVersion<Example> createExample(
      String exampleUuid,
      String pilotUuid,
      String firstName,
      String middleName,
      String lastName);
}
