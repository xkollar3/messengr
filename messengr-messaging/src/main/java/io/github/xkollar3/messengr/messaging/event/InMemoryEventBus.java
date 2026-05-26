package io.github.xkollar3.messengr.messaging.event;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import io.github.xkollar3.messengr.messaging.event.Event.Payload;

public class InMemoryEventBus implements EventBus {

  private final Map<Class<? extends Event.Payload>, Set<Event.Handler<?>>> handlers;

  public InMemoryEventBus(List<Event.Handler<?>> handlers) {
    var wildcardHandlers = handlers.stream()
        .filter(handler -> handler.eventType() == null)
        .collect(Collectors.toSet());

    this.handlers = handlers.stream()
        .filter(handler -> handler.eventType() != null)
        .collect(
            Collectors.groupingBy(handler -> handler.eventType(), Collectors.toSet()));

    if (!wildcardHandlers.isEmpty()) {
      this.handlers.forEach((_, evtHandlers) -> evtHandlers.addAll(wildcardHandlers));
    }
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
