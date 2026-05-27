package io.github.xkollar3.messengr.messengr_outbox;

import io.github.xkollar3.messengr.messaging.event.Event;
import io.github.xkollar3.messengr.messaging.event.Event.Payload;

/**
 * Wildcard handler which saves any event intercepted into the outbox table
 */
public class OutboxingEventHandler implements Event.Handler<Event.Payload> {

  private final MessageOutboxPort messageOutboxPort;

  public OutboxingEventHandler(MessageOutboxPort messageOutboxPort) {
    this.messageOutboxPort = messageOutboxPort;
  }

  @Override
  public Class<Payload> eventType() {
    return null;
  }

  @Override
  public void handle(Event.Payload event) {
    messageOutboxPort.outbox(event);
  }

  public MessageOutboxPort getMessageOutboxPort() {
    return messageOutboxPort;
  }
}
