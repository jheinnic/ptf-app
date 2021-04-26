package name.jchein.ptf.artlab.micros.component_registry.impl;

import java.util.UUID;

import org.junit.ClassRule;
import org.junit.Test;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.cluster.sharding.typed.javadsl.EntityContext;
import name.jchein.ptf.artlab.micros.component_registry.impl.ComponentRegistryCommand.ComponentRegistry;
import name.jchein.ptf.artlab.micros.component_registry.impl.ComponentRegistryCommand.UseGreetingMessage;

public class ComponentRegistryAggregateTest {
  private static final String inmemConfig =
      "akka.persistence.journal.plugin = \"akka.persistence.journal.inmem\" \n";

  private static final String snapshotConfig =
      "akka.persistence.snapshot-store.plugin = \"akka.persistence.snapshot-store.local\" \n"
      + "akka.persistence.snapshot-store.local.dir = \"target/snapshot-"
      + UUID.randomUUID().toString()
      + "\" \n";

  private static final String config = inmemConfig + snapshotConfig;

  @ClassRule
  public static final TestKitJunitResource testKit = new TestKitJunitResource(config);

  @Test
  public void testHello() {

      String id = "Alice";
      ActorRef<ComponentRegistryCommand> ref =
        testKit.spawn(
          ComponentRegistryAggregate.create(
            // Unit testing the Aggregate requires an EntityContext but starting
            // a complete Akka Cluster or sharding the actors is not requried.
            // The actorRef to the shard can be null as it won't be used.
            new EntityContext(ComponentRegistryAggregate.ENTITY_TYPE_KEY, id,  null)
          )
        );

      TestProbe<ComponentRegistryCommand.Greeting> probe =
        testKit.createTestProbe(ComponentRegistryCommand.Greeting.class);
      ref.tell(new ComponentRegistry(id,probe.getRef()));
      probe.expectMessage(new ComponentRegistryCommand.Greeting("Hello, Alice!"));
  }

  @Test
  public void testUpdateGreeting() {
      String id = "Alice";
      ActorRef<ComponentRegistryCommand> ref =
        testKit.spawn(
          ComponentRegistryAggregate.create(
            // Unit testing the Aggregate requires an EntityContext but starting
            // a complete Akka Cluster or sharding the actors is not requried.
            // The actorRef to the shard can be null as it won't be used.
           new EntityContext(ComponentRegistryAggregate.ENTITY_TYPE_KEY, id,  null)
          )
        );

      TestProbe<ComponentRegistryCommand.Confirmation> probe1 =
        testKit.createTestProbe(ComponentRegistryCommand.Confirmation.class);
      ref.tell(new UseGreetingMessage("Hi", probe1.getRef()));
      probe1.expectMessage(new ComponentRegistryCommand.Accepted());

      TestProbe<ComponentRegistryCommand.Greeting> probe2 =
        testKit.createTestProbe(ComponentRegistryCommand.Greeting.class);
      ref.tell(new ComponentRegistry(id,probe2.getRef()));
      probe2.expectMessage(new ComponentRegistryCommand.Greeting("Hi, Alice!"));
    }
}
