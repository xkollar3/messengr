package io.github.xkollar3.messengr.event;

public interface EventBus {

  <E extends Event.Payload> void publish(E event);
}
