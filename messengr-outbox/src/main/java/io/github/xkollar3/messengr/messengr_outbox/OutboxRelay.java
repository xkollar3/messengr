package io.github.xkollar3.messengr.messengr_outbox;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxRelay {

  private final OutboxRepository outboxRepository;
  private final MessageBackend messageBackend;

  public OutboxRelay(OutboxRepository outboxRepository, MessageBackend messageBackend) {
    this.outboxRepository = outboxRepository;
    this.messageBackend = messageBackend;
  }

  /**
   * Relays all currently persisted outbox events to the configured message
   * backend.
   */
  @Transactional
  public void relayPending() {
    var pendingEvents = outboxRepository.findAllByOrderByIdAsc();

    for (var pendingEvent : pendingEvents) {
      messageBackend.relay(new MessageBackend.JsonMessage(pendingEvent.getEventData()));
      outboxRepository.delete(pendingEvent);
    }
  }
}
