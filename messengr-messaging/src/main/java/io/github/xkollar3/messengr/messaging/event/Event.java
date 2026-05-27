package io.github.xkollar3.messengr.messaging.event;

import java.util.UUID;

public interface Event {

  public interface Handler<E extends Payload> {

    Class<E> eventType();

    void handle(E event);

  }

  public interface Payload {

    UUID id();

  }
}
