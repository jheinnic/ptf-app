package fixtures;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@ContextConfiguration(classes = {
   ExampleGrpcUnitTestConfiguration.class
}, loader = SpringBootContextLoader.class)
@TestPropertySource("classpath:/fixtures/ExampleGrpcUnitTest.properties")
@EnableAutoConfiguration(exclude= {
	io.eventuate.local.cdc.debezium.EventTableChangesToAggregateTopicRelayConfiguration.class
//	io.eventuate.local.java.jdbckafkastore.EventuateLocalConfiguration.class,
//	io.eventuate.local.postgres.wal.PostgresWalEventTableChangesToAggregateTopicTranslatorConfiguration.class,
//	io.eventuate.local.polling.PollingEventTableChangesToAggregateTopicTranslatorConfiguration.class,
//	io.eventuate.local.mysql.binlog.MySqlEventTableChangesToAggregateTopicTranslatorConfiguration.class
})
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableExampleGrpcUnitTest
{

}
