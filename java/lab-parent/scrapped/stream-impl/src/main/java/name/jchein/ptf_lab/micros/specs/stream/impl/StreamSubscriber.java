package name.jchein.ptf_lab.micros.specs.stream.impl;

import akka.Done;
import akka.stream.javadsl.Flow;
import name.jchein.ptf.artlab.micros.component_registry.api.ComponentRegistryService;
import name.jchein.ptf.artlab.micros.component_registry.impl.ComponentRegistryEvent;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

/**
 * This subscribes to the HelloService event stream.
 */
public class StreamSubscriber {

  @Inject
  public StreamSubscriber(ComponentRegistryService helloService, StreamRepository repository) {
    // Create a subscriber
    helloService.helloEvents().subscribe()
      // And subscribe to it with at least once processing semantics.
      .atLeastOnce(
        // Create a flow that emits a Done for each message it processes
        Flow.<ComponentRegistryEvent>create().mapAsync(1, event -> {

          if (event instanceof ComponentRegistryEvent.GreetingMessageChanged) {
            ComponentRegistryEvent.GreetingMessageChanged messageChanged = (ComponentRegistryEvent.GreetingMessageChanged) event;
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
