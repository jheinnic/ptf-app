package name.jchein.ptf.artlab.micros.reporting.reporting_stream.impl;

import akka.Done;
import akka.stream.javadsl.Flow;
import name.jchein.ptf.artlab.micros.reporting.reporting.api.ReportingEvent;
import name.jchein.ptf.artlab.micros.reporting.reporting.api.ReportingService;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

/**
 * This subscribes to the ReportingService event stream.
 */
public class StreamSubscriber {

  @Inject
  public StreamSubscriber(ReportingService reportingService, StreamRepository repository) {
    // Create a subscriber
    reportingService.helloEvents().subscribe()
      // And subscribe to it with at least once processing semantics.
      .atLeastOnce(
        // Create a flow that emits a Done for each message it processes
        Flow.<ReportingEvent>create().mapAsync(1, event -> {

          if (event instanceof ReportingEvent.GreetingMessageChanged) {
            ReportingEvent.GreetingMessageChanged messageChanged = (ReportingEvent.GreetingMessageChanged) event;
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
