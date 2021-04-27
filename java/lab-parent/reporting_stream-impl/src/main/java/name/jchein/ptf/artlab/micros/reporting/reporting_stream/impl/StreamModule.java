package name.jchein.ptf.artlab.micros.reporting.reporting_stream.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import name.jchein.ptf.artlab.micros.reporting.reporting.api.ReportingService;
import name.jchein.ptf.artlab.micros.reporting.reporting_stream.api.StreamService;

/**
 * The module that binds the StreamService so that it can be served.
 */
public class StreamModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    // Bind the StreamService service
    bindService(StreamService.class, StreamServiceImpl.class);
    // Bind the ReportingService client
    bindClient(ReportingService.class);
    // Bind the subscriber eagerly to ensure it starts up
    bind(StreamSubscriber.class).asEagerSingleton();
  }
}
