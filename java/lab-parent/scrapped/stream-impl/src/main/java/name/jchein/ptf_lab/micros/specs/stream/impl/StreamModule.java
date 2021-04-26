package name.jchein.ptf_lab.micros.specs.stream.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

import name.jchein.ptf.artlab.micros.component_registry.api.ComponentRegistryService;
import name.jchein.ptf_lab.micros.specs.stream.api.StreamService;

/**
 * The module that binds the StreamService so that it can be served.
 */
public class StreamModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    // Bind the StreamService service
    bindService(StreamService.class, StreamServiceImpl.class);
    // Bind the HelloService client
    bindClient(ComponentRegistryService.class);
    // Bind the subscriber eagerly to ensure it starts up
    bind(StreamSubscriber.class).asEagerSingleton();
  }
}
