package name.jchein.ptf.artlab.micros.component_registry.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import name.jchein.ptf.artlab.micros.component_registry.api.ComponentRegistryService;

/**
 * The module that binds the HelloService so that it can be served.
 */
public class ComponetRegisryModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    bindService(ComponentRegistryService.class, ComponentRegistryServiceImpl.class);
  }
}
