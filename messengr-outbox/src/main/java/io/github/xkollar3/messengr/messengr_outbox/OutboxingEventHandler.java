package io.github.xkollar3.messengr.messengr_outbox;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.xkollar3.messengr.messaging.event.Event;
import io.github.xkollar3.messengr.messaging.event.Event.Payload;

/**
 * Wildcard handler which saves any event intercepted into the outbox table
 */
@Component
public class OutboxingEventHandler implements Event.Handler<Event.Payload> {

  private final OutboxRepository outboxRepository;
  private final ObjectMapper objectMapper;

  public OutboxingEventHandler(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
    this.outboxRepository = outboxRepository;
    this.objectMapper = objectMapper;
  }

  @Override
  public Class<Payload> eventType() {
    return null;
  }

  @Override
  public void handle(Event.Payload event) {
    try {
      var serializedEvent = objectMapper.writeValueAsString(event);

      var entity = new EventOutboxEntity();
      entity.setEventData(serializedEvent);

      outboxRepository.save(entity);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
