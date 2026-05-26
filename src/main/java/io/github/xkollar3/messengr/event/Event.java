package io.github.xkollar3.messengr.event;

public interface Event {

  public interface Handler<E extends Payload> {

    public Class<E> eventType();

    public void handle(E event);
  }

  public interface Payload {

  }
}
