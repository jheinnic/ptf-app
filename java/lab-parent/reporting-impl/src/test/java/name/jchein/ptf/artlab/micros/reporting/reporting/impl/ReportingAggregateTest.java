package name.jchein.ptf.artlab.micros.reporting.reporting.impl;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.cluster.sharding.typed.javadsl.EntityContext;
import name.jchein.ptf.artlab.micros.reporting.reporting.impl.ReportingCommand.Hello;
import name.jchein.ptf.artlab.micros.reporting.reporting.impl.ReportingCommand.UseGreetingMessage;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.UUID;

public class ReportingAggregateTest {
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
      ActorRef<ReportingCommand> ref =
        testKit.spawn(
          ReportingAggregate.create(
            // Unit testing the Aggregate requires an EntityContext but starting
            // a complete Akka Cluster or sharding the actors is not requried.
            // The actorRef to the shard can be null as it won't be used.
            new EntityContext(ReportingAggregate.ENTITY_TYPE_KEY, id,  null)
          )
        );

      TestProbe<ReportingCommand.Greeting> probe =
        testKit.createTestProbe(ReportingCommand.Greeting.class);
      ref.tell(new Hello(id,probe.getRef()));
      probe.expectMessage(new ReportingCommand.Greeting("Hello, Alice!"));
  }

  @Test
  public void testUpdateGreeting() {
      String id = "Alice";
      ActorRef<ReportingCommand> ref =
        testKit.spawn(
          ReportingAggregate.create(
            // Unit testing the Aggregate requires an EntityContext but starting
            // a complete Akka Cluster or sharding the actors is not requried.
            // The actorRef to the shard can be null as it won't be used.
           new EntityContext(ReportingAggregate.ENTITY_TYPE_KEY, id,  null)
          )
        );

      TestProbe<ReportingCommand.Confirmation> probe1 =
        testKit.createTestProbe(ReportingCommand.Confirmation.class);
      ref.tell(new UseGreetingMessage("Hi", probe1.getRef()));
      probe1.expectMessage(new ReportingCommand.Accepted());

      TestProbe<ReportingCommand.Greeting> probe2 =
        testKit.createTestProbe(ReportingCommand.Greeting.class);
      ref.tell(new Hello(id,probe2.getRef()));
      probe2.expectMessage(new ReportingCommand.Greeting("Hi, Alice!"));
    }
}
