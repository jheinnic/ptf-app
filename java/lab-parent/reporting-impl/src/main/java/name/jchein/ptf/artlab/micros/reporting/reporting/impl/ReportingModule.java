package name.jchein.ptf.artlab.micros.reporting.reporting.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import name.jchein.ptf.artlab.micros.reporting.reporting.api.ReportingService;

/**
 * The module that binds the ReportingService so that it can be served.
 */
public class ReportingModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    bindService(ReportingService.class, ReportingServiceImpl.class);
  }
}
