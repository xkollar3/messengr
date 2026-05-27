package io.github.xkollar3.messengr.messengr_outbox;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.xkollar3.messengr.messaging.event.Event;

public class OutboxRelay {

  private static final Logger LOGGER = Logger.getLogger(OutboxRelay.class.getName());

  private final MessageOutboxPort messageOutboxPort;
  private final MessageRelayPort relayPort;

  private final long relayPerCycleSize;

  public OutboxRelay(MessageOutboxPort messageOutboxPort, MessageRelayPort relayPort,
      ScheduledExecutorService relayExecutor,
      long relayPerCycleSize,
      long outboxCycleMs) {
    this.messageOutboxPort = messageOutboxPort;
    this.relayPort = relayPort;
    this.relayPerCycleSize = relayPerCycleSize;

    relayExecutor.schedule(this::relayPending, outboxCycleMs, TimeUnit.MILLISECONDS);
  }

  /**
   * Relays all currently persisted outbox events to the configured message
   * backend.
   */
  public synchronized void relayPending() {
    LOGGER.info("Relaying pending events");
    List<Event.Payload> eventsToRelay = messageOutboxPort.getEventsToRelay(relayPerCycleSize);

    List<Event.Payload> successfullyRelayed = eventsToRelay.stream().filter(event -> relayInternal(event)).toList();

    LOGGER.info("Relayed: " + successfullyRelayed.size() + ", of: " + eventsToRelay.size() + " events");

    messageOutboxPort.markEventsAsRelayed(successfullyRelayed);
  }

  private boolean relayInternal(Event.Payload event) {
    boolean ok = relayPort.relay(event);

    LOGGER.log(
        Level.FINE,
        "Relayed event of type: {0}, id: {1}",
        new Object[] { event.getClass().getSimpleName(), event.id() });

    return ok;
  }
}
