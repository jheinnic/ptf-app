package name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.api;

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

/**
 * The ProtocolRegistry service interface.
 * <p>
 * This describes everything that Lagom needs to know about how to serve and
 * consume the ProtocolRegistry.
 */
public interface ProtocolRegistryService extends Service {

  /**
   * Example: curl http://localhost:9000/api/protocol_registry/Alice
   */
  ServiceCall<NotUsed, GetCanvasSizeReply> getCanvasSize(String id);


  /**
   * Example: curl -H "Content-Type: application/json" -X POST -d '{"message":
   * "Hi"}' http://localhost:9000/api/protocol_registry
   */
  ServiceCall<CreateCanvasSizeMessage, CanvasSizeCreatedReply> createCanvasSize();


  /**
   * Example: curl -H "Content-Type: application/json" -X POST -d '{"message":
   * "Hi"}' http://localhost:9000/api/protocol_registry
   */
  ServiceCall<UpdateCanvasSizeDisplayNameMessage, Done> updateCanvasSizeDisplayName(String id);


  /**
   * This gets published to Kafka.
   */
  Topic<CanvasSizeEvent> helloEvents();

  @Override
  default Descriptor descriptor() {
    // @formatter:off
    return named("protocol_registry").withCalls(
        pathCall("/api/canvas_sizes/:id",  this::getCanvasSize),
        pathCall("/api/canvas_sizes", this::createCanvasSize),
        pathCall("/api/canvas_sizes/:id",  this::updateCanvasSizeDisplayName)
      ).withTopics(
          topic("canvas-size-events", this::helloEvents)
          // Kafka partitions messages, messages within the same partition will
          // be delivered in order, to ensure that all messages for the same user
          // go to the same partition (and hence are delivered in order with respect
          // to that user), we configure a partition key strategy that extracts the
          // name as the partition key.
          .withProperty(KafkaProperties.partitionKeyStrategy(), CanvasSizeEvent::getName)
        ).withAutoAcl(true);
    // @formatter:on
  }
}
