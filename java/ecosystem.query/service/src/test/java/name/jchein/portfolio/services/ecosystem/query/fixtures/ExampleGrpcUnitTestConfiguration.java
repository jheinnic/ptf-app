package name.jchein.portfolio.services.ecosystem.query.fixtures;


import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;

import io.grpc.Channel;
import net.devh.springboot.autoconfigure.grpc.client.GrpcClient;

import name.jchein.portfolio.services.ecosystem.query.grpc.proto.ExampleGrpc;


@TestConfiguration
@ComponentScan
public class ExampleGrpcUnitTestConfiguration
{
	@GrpcClient("ecosystem_query")
	Channel clientChannel;
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	ExampleGrpc.ExampleBlockingStub exampleBlockingClient() {
		return ExampleGrpc.newBlockingStub(this.clientChannel);
	}
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	ExampleGrpc.ExampleFutureStub exampleFutureClient() {
		return ExampleGrpc.newFutureStub(this.clientChannel);
	}
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	ExampleGrpc.ExampleStub exampleClient() {
		return ExampleGrpc.newStub(this.clientChannel);
	}
}
