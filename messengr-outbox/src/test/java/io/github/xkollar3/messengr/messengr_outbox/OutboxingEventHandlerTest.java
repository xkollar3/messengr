package io.github.xkollar3.messengr.messengr_outbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.github.xkollar3.messengr.messaging.event.Event;
import io.github.xkollar3.messengr.messaging.event.EventBus;
import io.github.xkollar3.messengr.messaging.event.InMemoryEventBus;

public class OutboxingEventHandlerTest {

  @Test
  void publishEvent_withOutboxingHandler_eventsAreRelayed() {
    InMemoryTestMessageOutboxAdapter outbox = new InMemoryTestMessageOutboxAdapter(2);
    InMemoryTestMessageRelayAdapter relay = new InMemoryTestMessageRelayAdapter();
    OutboxingEventHandler outboxingHandler = new OutboxingEventHandler(outbox);
    EventBus eventBus = new InMemoryEventBus(List.of(outboxingHandler, new NoOpTypedTestEventHandler()));
    OutboxRelay outboxRelay = createOutboxRelay(outbox, relay);

    TestEvent first = new TestEvent(UUID.randomUUID());
    TestEvent second = new TestEvent(UUID.randomUUID());

    eventBus.publish(first);
    eventBus.publish(second);

    assertTrue(outbox.awaitOutboxed());

    outboxRelay.relayPending();

    assertEquals(Set.of(first.id(), second.id()), relay.relayedEventIds());
    assertEquals(0, outbox.pendingCount());
  }

  @Test
  void relayPending_relayFailsFirstTime_eventIsRetriedAndEventuallyRelayed() {
    InMemoryTestMessageOutboxAdapter outbox = new InMemoryTestMessageOutboxAdapter(1);
    InMemoryTestMessageRelayAdapter relay = new InMemoryTestMessageRelayAdapter();
    OutboxingEventHandler outboxingHandler = new OutboxingEventHandler(outbox);
    EventBus eventBus = new InMemoryEventBus(List.of(outboxingHandler, new NoOpTypedTestEventHandler()));
    OutboxRelay outboxRelay = createOutboxRelay(outbox, relay);

    TestEvent event = new TestEvent(UUID.randomUUID());
    relay.failFirstAttempt(event.id());

    eventBus.publish(event);
    assertTrue(outbox.awaitOutboxed());

    outboxRelay.relayPending();

    assertEquals(1, relay.attemptsFor(event.id()));
    assertTrue(relay.relayedEventIds().isEmpty());
    assertEquals(1, outbox.pendingCount());

    outboxRelay.relayPending();

    assertEquals(2, relay.attemptsFor(event.id()));
    assertEquals(Set.of(event.id()), relay.relayedEventIds());
    assertEquals(0, outbox.pendingCount());
  }

  private OutboxRelay createOutboxRelay(MessageOutboxPort outbox, MessageRelayPort relay) {
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    OutboxRelay outboxRelay = new OutboxRelay(outbox, relay, executor, 100, TimeUnit.DAYS.toMillis(1));
    executor.shutdownNow();
    return outboxRelay;
  }

  private static class TestEvent implements Event.Payload {

    private final UUID id;

    private TestEvent(UUID id) {
      this.id = id;
    }

    @Override
    public UUID id() {
      return id;
    }

  }

  private static class NoOpTypedTestEventHandler implements Event.Handler<TestEvent> {

    @Override
    public Class<TestEvent> eventType() {
      return TestEvent.class;
    }

    @Override
    public void handle(TestEvent event) {
    }
  }

  private static class InMemoryTestMessageOutboxAdapter implements MessageOutboxPort {

    private final List<Event.Payload> pendingEvents = new ArrayList<>();
    private final CountDownLatch outboxedLatch;

    private InMemoryTestMessageOutboxAdapter(int expectedOutboxedEvents) {
      this.outboxedLatch = new CountDownLatch(expectedOutboxedEvents);
    }

    @Override
    public synchronized void outbox(Event.Payload payload) {
      pendingEvents.add(payload);
      outboxedLatch.countDown();
    }

    @Override
    public synchronized List<Event.Payload> getEventsToRelay(Long amount) {
      int max = Math.min(amount.intValue(), pendingEvents.size());
      return new ArrayList<>(pendingEvents.subList(0, max));
    }

    @Override
    public synchronized void markEventsAsRelayed(List<Event.Payload> events) {
      pendingEvents.removeAll(events);
    }

    synchronized int pendingCount() {
      return pendingEvents.size();
    }

    boolean awaitOutboxed() {
      try {
        return outboxedLatch.await(1, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return false;
      }
    }
  }

  private static class InMemoryTestMessageRelayAdapter implements MessageRelayPort {

    private final Set<UUID> failFirstAttemptFor = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<UUID, Integer> attemptsByEventId = new ConcurrentHashMap<>();
    private final List<Event.Payload> relayedEvents = new ArrayList<>();

    @Override
    public synchronized boolean relay(Event.Payload event) {
      UUID eventId = event.id();
      int attempts = attemptsByEventId.merge(eventId, 1, Integer::sum);

      if (failFirstAttemptFor.contains(eventId) && attempts == 1) {
        return false;
      }

      relayedEvents.add(event);
      return true;
    }

    void failFirstAttempt(UUID eventId) {
      failFirstAttemptFor.add(eventId);
    }

    int attemptsFor(UUID eventId) {
      return attemptsByEventId.getOrDefault(eventId, 0);
    }

    synchronized Set<UUID> relayedEventIds() {
      return relayedEvents.stream().map(Event.Payload::id).collect(Collectors.toSet());
    }
  }

}
