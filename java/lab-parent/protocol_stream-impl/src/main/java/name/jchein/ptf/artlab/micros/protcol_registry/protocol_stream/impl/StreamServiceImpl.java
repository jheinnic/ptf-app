package name.jchein.ptf.artlab.micros.protcol_registry.protocol_stream.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.api.ProtocolRegistryService;
import name.jchein.ptf.artlab.micros.protcol_registry.protocol_stream.api.StreamService;

import javax.inject.Inject;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Implementation of the HelloString.
 */
public class StreamServiceImpl implements StreamService {

  private final ProtocolRegistryService protocol_registryService;
  private final StreamRepository repository;

  @Inject
  public StreamServiceImpl(ProtocolRegistryService protocol_registryService, StreamRepository repository) {
    this.protocol_registryService = protocol_registryService;
    this.repository = repository;
  }

  @Override
  public ServiceCall<Source<String, NotUsed>, Source<String, NotUsed>> directStream() {
    return hellos -> completedFuture(
      hellos.mapAsync(8, name ->  protocol_registryService.hello(name).invoke()));
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
