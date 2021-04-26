package name.jchein.ptf_lab.micros.specs.stream.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import name.jchein.ptf.artlab.micros.component_registry.api.ComponentRegistryService;
import name.jchein.ptf_lab.micros.specs.stream.api.StreamService;

import javax.inject.Inject;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Implementation of the HelloString.
 */
public class StreamServiceImpl implements StreamService {

  private final ComponentRegistryService helloService;
  private final StreamRepository repository;

  @Inject
  public StreamServiceImpl(ComponentRegistryService helloService, StreamRepository repository) {
    this.helloService = helloService;
    this.repository = repository;
  }

  @Override
  public ServiceCall<Source<String, NotUsed>, Source<String, NotUsed>> directStream() {
    return hellos -> completedFuture(
      hellos.mapAsync(8, name ->  helloService.hello(name).invoke()));
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
