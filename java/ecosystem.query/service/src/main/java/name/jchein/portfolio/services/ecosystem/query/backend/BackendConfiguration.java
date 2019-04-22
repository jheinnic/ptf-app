package name.jchein.portfolio.services.ecosystem.query.backend;


import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import io.eventuate.sync.AggregateRepository;
import name.jchein.portfolio.services.ecosystem.query.domain.Example;
import name.jchein.portfolio.services.ecosystem.query.domain.ExampleCommand;
import name.jchein.portfolio.common.es.eventuate.IUUIDExtension;


@Configuration
public class BackendConfiguration
{
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	IExampleDomainService exampleDomainService(
		AggregateRepository<Example, ExampleCommand> exampleRepository,
		IUUIDExtension uuidExtension )
	{
		return new ExampleDomainService(exampleRepository, uuidExtension);
	}
}
