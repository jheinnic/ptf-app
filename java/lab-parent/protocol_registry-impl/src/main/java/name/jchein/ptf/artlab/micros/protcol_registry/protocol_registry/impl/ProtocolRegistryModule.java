package name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.api.ProtocolRegistryService;

/**
 * The module that binds the ProtocolRegistryService so that it can be served.
 */
public class ProtocolRegistryModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    bindService(ProtocolRegistryService.class, ProtocolRegistryServiceImpl.class);
    bind
  }
}
