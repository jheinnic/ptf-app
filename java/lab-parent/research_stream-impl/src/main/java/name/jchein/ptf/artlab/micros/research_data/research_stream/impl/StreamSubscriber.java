package name.jchein.ptf.artlab.micros.research_data.research_stream.impl;

import akka.Done;
import akka.stream.javadsl.Flow;
import name.jchein.ptf.artlab.micros.research_data.research_data.api.ResearchDataEvent;
import name.jchein.ptf.artlab.micros.research_data.research_data.api.ResearchDataService;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

/**
 * This subscribes to the ResearchDataService event stream.
 */
public class StreamSubscriber {

  @Inject
  public StreamSubscriber(ResearchDataService research_dataService, StreamRepository repository) {
    // Create a subscriber
    research_dataService.helloEvents().subscribe()
      // And subscribe to it with at least once processing semantics.
      .atLeastOnce(
        // Create a flow that emits a Done for each message it processes
        Flow.<ResearchDataEvent>create().mapAsync(1, event -> {

          if (event instanceof ResearchDataEvent.GreetingMessageChanged) {
            ResearchDataEvent.GreetingMessageChanged messageChanged = (ResearchDataEvent.GreetingMessageChanged) event;
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
