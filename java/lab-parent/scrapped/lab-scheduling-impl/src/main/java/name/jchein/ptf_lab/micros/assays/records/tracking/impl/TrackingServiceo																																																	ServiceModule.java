package name.jchein.ptf_lab.micros.assays.records.tracking.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import name.jchein.ptf_lab.micros.assays.records.tracking.api.TrackingServiceo																																																	ServiceService;

/**
 * The module that binds the TrackingServiceo																																																	ServiceService so that it can be served.
 */
public class TrackingServiceo																																																	ServiceModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    bindService(TrackingServiceo																																																	ServiceService.class, TrackingServiceo																																																	ServiceServiceImpl.class);
  }
}
