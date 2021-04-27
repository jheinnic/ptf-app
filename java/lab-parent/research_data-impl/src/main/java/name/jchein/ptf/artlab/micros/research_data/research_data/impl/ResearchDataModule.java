package name.jchein.ptf.artlab.micros.research_data.research_data.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import name.jchein.ptf.artlab.micros.research_data.research_data.api.ResearchDataService;

/**
 * The module that binds the ResearchDataService so that it can be served.
 */
public class ResearchDataModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    bindService(ResearchDataService.class, ResearchDataServiceImpl.class);
  }
}
