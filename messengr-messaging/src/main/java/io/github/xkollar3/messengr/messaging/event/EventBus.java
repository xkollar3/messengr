package io.github.xkollar3.messengr.messaging.event;

public interface EventBus {

  <E extends Event.Payload> void publish(E event);
}
