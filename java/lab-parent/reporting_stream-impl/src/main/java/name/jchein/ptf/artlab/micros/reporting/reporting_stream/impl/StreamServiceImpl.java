package name.jchein.ptf.artlab.micros.reporting.reporting_stream.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import name.jchein.ptf.artlab.micros.reporting.reporting.api.ReportingService;
import name.jchein.ptf.artlab.micros.reporting.reporting_stream.api.StreamService;

import javax.inject.Inject;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Implementation of the HelloString.
 */
public class StreamServiceImpl implements StreamService {

  private final ReportingService reportingService;
  private final StreamRepository repository;

  @Inject
  public StreamServiceImpl(ReportingService reportingService, StreamRepository repository) {
    this.reportingService = reportingService;
    this.repository = repository;
  }

  @Override
  public ServiceCall<Source<String, NotUsed>, Source<String, NotUsed>> directStream() {
    return hellos -> completedFuture(
      hellos.mapAsync(8, name ->  reportingService.hello(name).invoke()));
  }

  @Override
  public ServiceCall<Source<String, NotUsed>, Source<String, NotUsed>> autonomousStream() {
    return hellos -> completedFuture(
        hellos.mapAsync(8, name -> repository.getMessage(name).thenApply( message ->
            String.format("%s, %s!", message.orElse("Hello"), name)
        ))
    );
  }
}
