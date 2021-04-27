package name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.impl;

import akka.cluster.sharding.typed.javadsl.EntityContext;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.*;
import com.lightbend.lagom.javadsl.persistence.AkkaTaggerAdapter;
import name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.impl.CanvasSizeCommand.Hello;
import name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.impl.CanvasSizeCommand.UseGreetingMessage;
import name.jchein.ptf.artlab.micros.protcol_registry.protocol_registry.impl.CanvasSizeEvent.GreetingMessageChanged;

import java.util.Set;

/**
* This is an event sourced aggregate. It has a state, {@link CanvasSizeState}, which
* stores what the greeting should be (eg, "Hello").
* <p>
* Event sourced aggregate are interacted with by sending them commands. This
* aggregate supports two commands, a {@link UseGreetingMessage} command, which is
* used to change the greeting, and a {@link Hello} command, which is a read
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
public class CanvasSizeAggregate extends EventSourcedBehaviorWithEnforcedReplies<CanvasSizeCommand, CanvasSizeEvent, CanvasSizeState> {

  public static EntityTypeKey<CanvasSizeCommand> ENTITY_TYPE_KEY =
    EntityTypeKey
      .create(CanvasSizeCommand.class, "CanvasSizeAggregate");


  final private EntityContext<CanvasSizeCommand> entityContext;
  final private String entityId;

  CanvasSizeAggregate(EntityContext<CanvasSizeCommand> entityContext) {
    super(
      PersistenceId.of(
          entityContext.getEntityTypeKey().name(),
          entityContext.getEntityId()
        )
      );
    this.entityContext = entityContext;
    this.entityId = entityContext.getEntityId();
  }

  public static CanvasSizeAggregate create(EntityContext<CanvasSizeCommand> entityContext) {
    return new CanvasSizeAggregate(entityContext);
  }

  @Override
  public CanvasSizeState emptyState() {
    return CanvasSizeState.INITIAL;
  }


  @Override
  public CommandHandlerWithReply<CanvasSizeCommand, CanvasSizeEvent, CanvasSizeState> commandHandler() {

    CommandHandlerWithReplyBuilder<CanvasSizeCommand, CanvasSizeEvent, CanvasSizeState> builder = newCommandHandlerWithReplyBuilder();

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
          .thenReply(cmd.replyTo, __ -> new CanvasSizeCommand.Accepted())
      );

    /*
     * Command handler for the Hello command.
     */
    builder.forAnyState()
      .onCommand(Hello.class, (state, cmd) ->
        Effect().none()
          // Get the greeting from the current state, and prepend it to the name
          // that we're sending a greeting to, and reply with that message.
          .thenReply(cmd.replyTo, __ -> new CanvasSizeCommand.Greeting(state.message + ", " + cmd.name + "!"))
      );

    return builder.build();

  }


  @Override
  public EventHandler<CanvasSizeState, CanvasSizeEvent> eventHandler() {
    EventHandlerBuilder<CanvasSizeState, CanvasSizeEvent> builder = newEventHandlerBuilder();

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
  public Set<String> tagsFor(CanvasSizeEvent shoppingCartEvent) {
    return AkkaTaggerAdapter.fromLagom(entityContext, CanvasSizeEvent.TAG).apply(shoppingCartEvent);
  }

}
