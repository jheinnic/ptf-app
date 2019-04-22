package name.jchein.portfolio.services.paint.gateway.domain;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import io.eventuate.javaclient.spring.EnableEventHandlers;
import io.eventuate.sync.AggregateRepository;
import io.eventuate.sync.EventuateAggregateStore;

import name.jchein.common.identity.IUUIDGenerator;


@Configuration
@EnableEventHandlers
public class DomainConfiguration
{
   @Bean
   @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
   AggregateRepository<Example, ExampleCommand> exampleAggregateRepository(EventuateAggregateStore eventStore) {
      return new AggregateRepository<Example, ExampleCommand>(Example.class, eventStore);
   }
   
   @Bean
   @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
   NewExampleCreationHandler newExampleWorkflow(@Autowired IUUIDGenerator uuidGenerator)
   {
      return new NewExampleCreationHandler(uuidGenerator);
   }
}
