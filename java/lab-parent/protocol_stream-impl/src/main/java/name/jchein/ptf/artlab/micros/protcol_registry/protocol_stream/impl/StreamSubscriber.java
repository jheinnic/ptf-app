package name.jchein.ptf.artlab.micros.protcol_registry.protocol_stream.impl;

import akka.Done;
import akka.stream.javadsl.Flow;
import name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.api.CanvasSizeEvent;
import name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.api.ProtocolRegistryService;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

/**
 * This subscribes to the ProtocolRegistryService event stream.
 */
public class StreamSubscriber {

  @Inject
  public StreamSubscriber(ProtocolRegistryService protocol_registryService, StreamRepository repository) {
    // Create a subscriber
    protocol_registryService.helloEvents().subscribe()
      // And subscribe to it with at least once processing semantics.
      .atLeastOnce(
        // Create a flow that emits a Done for each message it processes
        Flow.<CanvasSizeEvent>create().mapAsync(1, event -> {

          if (event instanceof CanvasSizeEvent.GreetingMessageChanged) {
            CanvasSizeEvent.GreetingMessageChanged messageChanged = (CanvasSizeEvent.GreetingMessageChanged) event;
            // Update the message
            return repository.updateMessage(messageChanged.getName(), messageChanged.getMessage());

          } else {
            // Ignore all other events
            return CompletableFuture.completedFuture(Done.getInstance());
          }
        })
      );

  }
}
