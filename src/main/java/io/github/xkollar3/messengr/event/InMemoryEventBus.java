package io.github.xkollar3.messengr.event;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import io.github.xkollar3.messengr.event.Event.Payload;

public class InMemoryEventBus implements EventBus {

  private final Map<Class<? extends Event.Payload>, Set<Event.Handler<?>>> handlers;

  public InMemoryEventBus(List<Event.Handler<?>> handlers) {
    this.handlers = handlers.stream().collect(Collectors.groupingBy(Event.Handler::eventType, Collectors.toSet()));
  }

  @Override
  @SuppressWarnings("unchecked")
  public <E extends Payload> void publish(E event) {
    var eventClass = event.getClass();

    var handlersForEvent = handlers.get(eventClass);
    if (handlersForEvent == null) {
      return;
    }

    handlersForEvent.forEach(handler -> CompletableFuture.runAsync(() -> ((Event.Handler<E>) handler).handle(event)));
  }
}
