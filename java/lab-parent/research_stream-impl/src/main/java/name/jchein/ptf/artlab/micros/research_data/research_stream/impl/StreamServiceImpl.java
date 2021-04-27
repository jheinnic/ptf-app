package name.jchein.ptf.artlab.micros.research_data.research_stream.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import name.jchein.ptf.artlab.micros.research_data.research_data.api.ResearchDataService;
import name.jchein.ptf.artlab.micros.research_data.research_stream.api.StreamService;

import javax.inject.Inject;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Implementation of the HelloString.
 */
public class StreamServiceImpl implements StreamService {

  private final ResearchDataService research_dataService;
  private final StreamRepository repository;

  @Inject
  public StreamServiceImpl(ResearchDataService research_dataService, StreamRepository repository) {
    this.research_dataService = research_dataService;
    this.repository = repository;
  }

  @Override
  public ServiceCall<Source<String, NotUsed>, Source<String, NotUsed>> directStream() {
    return hellos -> completedFuture(
      hellos.mapAsync(8, name ->  research_dataService.hello(name).invoke()));
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
