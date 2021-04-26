package name.jchein.ptf.artlab.micros.component_registry.impl;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import name.jchein.ptf.artlab.micros.component_registry.api.GreetingMessage;
import name.jchein.ptf.artlab.micros.component_registry.api.ComponentRegistryService;

public class ComponentRegistryServiceTest {

  @Test
  public void shouldStorePersonalizedGreeting() throws Exception {
    withServer(defaultSetup().withCassandra(), server -> {
      ComponentRegistryService service = server.client(ComponentRegistryService.class);

      String msg1 = service.component("Alice").invoke().toCompletableFuture().get(5, SECONDS);
      assertEquals("Hello, Alice!", msg1); // default greeting

      service.useGreeting("Alice")
          .invoke(
    		  new GreetingMessage("Hi")
    	  ).toCompletableFuture()
          .get(5, SECONDS);
      String msg2 = service.component("Alice").invoke().toCompletableFuture().get(5, SECONDS);
      assertEquals("Hi, Alice!", msg2);

      String msg3 = service.component("Bob").invoke().toCompletableFuture().get(5, SECONDS);
      assertEquals("Hello, Bob!", msg3); // default greeting
    });
  }

}
