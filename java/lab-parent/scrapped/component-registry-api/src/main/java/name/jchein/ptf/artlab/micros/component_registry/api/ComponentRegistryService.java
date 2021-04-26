package name.jchein.ptf.artlab.micros.component_registry.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;
import static com.lightbend.lagom.javadsl.api.Service.topic;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.broker.kafka.KafkaProperties;
import name.jchein.ptf.artlab.micros.component_registry.api.GreetingMessage;
import name.jchein.ptf.artlab.micros.component_registry.api.ComponentRegistryEvent;

/**
 * The HelloService service interface.
 * <p>
 * This describes everything that Lagom needs to know about how to serve and
 * consume the HelloService.
 */
public interface ComponentRegistryService extends Service {

  /**
   * Example: curl http://localhost:9000/api/components/Alice
   */
  ServiceCall<NotUsed, String> component(String id);


  /**
   * Example: curl -H "Content-Type: application/json" -X POST -d '{"message":
   * "Hi"}' http://localhost:9000/api/components/Alice
   */
  ServiceCall<GreetingMessage, Done> useGreeting(String id);


  /**
   * This gets published to Kafka.
   */
  Topic<ComponentRegistryEvent> componentEvents();

  @Override
  default Descriptor descriptor() {
    // @formatter:off
    return named("components").withCalls(
        pathCall("/api/components/:id",  this::component),
        pathCall("/api/components/:id", this::useGreeting)
      ).withTopics(
          topic("component-events", this::componentEvents)
          // Kafka partitions messages, messages within the same partition will
          // be delivered in order, to ensure that all messages for the same user
          // go to the same partition (and hence are delivered in order with respect
          // to that user), we configure a partition key strategy that extracts the
          // name as the partition key.
          .withProperty(KafkaProperties.partitionKeyStrategy(), ComponentRegistryEvent::getName)
        ).withAutoAcl(true);
    // @formatter:on
  }
}
