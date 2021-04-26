package name.jchein.ptf.artlab.micros.component_registry.impl;

import akka.cluster.sharding.typed.javadsl.EntityContext;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.*;
import name.jchein.ptf.artlab.micros.component_registry.impl.ComponentRegistryEvent;
import name.jchein.ptf.artlab.micros.component_registry.impl.ComponentRegistryEvent.GreetingMessageChanged;
import name.jchein.ptf.artlab.micros.component_registry.impl.ComponentRegistryCommand.ComponentRegistry;
import name.jchein.ptf.artlab.micros.component_registry.impl.ComponentRegistryCommand.UseGreetingMessage;

import com.lightbend.lagom.javadsl.persistence.AkkaTaggerAdapter;

import java.util.Set;

/**
* This is an event sourced aggregate. It has a state, {@link ComponentRegistryState}, which
* stores what the greeting should be (eg, "Hello").
* <p>
* Event sourced aggregate are interacted with by sending them commands. This
* aggregate supports two commands, a {@link UseGreetingMessage} command, which is
* used to change the greeting, and a {@link ComponentRegistry} command, which is a read
* only command which returns a greeting to the name specified by the command.
* <p>
* Commands may emit events, and it's the events that get persisted.
* Each event will have an event handler registered for it, and an
* event handler simply applies an event to the current state. This will be done
* when the event is first created, and it will also be done when the entity is
* loaded from the database - each event will be replayed to recreate the state
* of the aggregate.
* <p>
* This aggregate defines one event, the {@link GreetingMessageChanged} event,
* which is emitted when a {@link UseGreetingMessage} command is received.
*/
public class ComponentRegistryAggregate extends EventSourcedBehaviorWithEnforcedReplies<ComponentRegistryCommand, ComponentRegistryEvent, ComponentRegistryState> {

  public static EntityTypeKey<ComponentRegistryCommand> ENTITY_TYPE_KEY =
    EntityTypeKey
      .create(ComponentRegistryCommand.class, "HelloAggregate");


  final private EntityContext<ComponentRegistryCommand> entityContext;
  final private String entityId;

  ComponentRegistryAggregate(EntityContext<ComponentRegistryCommand> entityContext) {
    super(
      PersistenceId.of(
          entityContext.getEntityTypeKey().name(),
          entityContext.getEntityId()
        )
      );
    this.entityContext = entityContext;
    this.entityId = entityContext.getEntityId();
  }

  public static ComponentRegistryAggregate create(EntityContext<ComponentRegistryCommand> entityContext) {
    return new ComponentRegistryAggregate(entityContext);
  }

  @Override
  public ComponentRegistryState emptyState() {
    return ComponentRegistryState.INITIAL;
  }


  @Override
  public CommandHandlerWithReply<ComponentRegistryCommand, ComponentRegistryEvent, ComponentRegistryState> commandHandler() {

    CommandHandlerWithReplyBuilder<ComponentRegistryCommand, ComponentRegistryEvent, ComponentRegistryState> builder = newCommandHandlerWithReplyBuilder();

    /*
     * Command handler for the UseGreetingMessage command.
     */
    builder.forAnyState()
      .onCommand(UseGreetingMessage.class, (state, cmd) ->
        Effect()
          // In response to this command, we want to first persist it as a
          // GreetingMessageChanged event
          .persist(new GreetingMessageChanged(entityId, cmd.message))
          // Then once the event is successfully persisted, we respond with done.
          .thenReply(cmd.replyTo, __ -> new ComponentRegistryCommand.Accepted())
      );

    /*
     * Command handler for the Hello command.
     */
    builder.forAnyState()
      .onCommand(ComponentRegistry.class, (state, cmd) ->
        Effect().none()
          // Get the greeting from the current state, and prepend it to the name
          // that we're sending a greeting to, and reply with that message.
          .thenReply(cmd.replyTo, __ -> new ComponentRegistryCommand.Greeting(state.message + ", " + cmd.name + "!"))
      );

    return builder.build();

  }


  @Override
  public EventHandler<ComponentRegistryState, ComponentRegistryEvent> eventHandler() {
    EventHandlerBuilder<ComponentRegistryState, ComponentRegistryEvent> builder = newEventHandlerBuilder();

    /*
     * Event handler for the GreetingMessageChanged event.
     */
    builder.forAnyState()
      .onEvent(GreetingMessageChanged.class, (state, evt) ->
        // We simply update the current state to use the greeting message from
        // the event.
        state.withMessage(evt.message)
      );
    return builder.build();
  }


  @Override
  public Set<String> tagsFor(ComponentRegistryEvent shoppingCartEvent) {
    return AkkaTaggerAdapter.fromLagom(entityContext, ComponentRegistryEvent.TAG).apply(shoppingCartEvent);
  }

}
