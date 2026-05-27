package io.github.xkollar3.messengr.messaging.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

public class InMemoryEventBusTest {

  @Test
  void eventBusConstruction_validEventBus_busConstructed() {
    var handler = new TestEventHandler(new CountDownLatch(0));

    new InMemoryEventBus(List.of(handler));

    assertTrue(true);
  }

  @Test
  void publishEvent_handlerFound_publishSuccessful() {
    var latch = new CountDownLatch(1);
    var handler = new TestEventHandler(latch);
    EventBus bus = new InMemoryEventBus(List.of(handler));
    var eventId = UUID.randomUUID();

    bus.publish(new TestEvent(eventId, "Hello world"));

    assertTrue(await(latch));
    assertEquals("Hello world", handler.handledPayload().get());
    assertEquals(eventId, handler.handledEventId().get());
  }

  @Test
  void publishEvent_noHandler_publishSuccessful() {
    EventBus emptyBus = new InMemoryEventBus(List.of());

    var event = new TestEvent(UUID.randomUUID(), "Hello world");
    assertDoesNotThrow(() -> emptyBus.publish(event));
  }

  @Test
  void publishEvent_twoHandlersForEvent_bothHandlersCalled() {
    var counter = new AtomicInteger(0);
    var latch = new CountDownLatch(2);
    var firstHandledEventId = new AtomicReference<UUID>();
    var secondHandledEventId = new AtomicReference<UUID>();
    var eventId = UUID.randomUUID();

    EventBus bus = new InMemoryEventBus(
        List.of(
            new CountingEventHandler(counter, latch, firstHandledEventId),
            new CountingEventHandler(counter, latch, secondHandledEventId)));

    bus.publish(new TestEvent(eventId, "Hello world"));

    assertTrue(await(latch));
    assertEquals(2, counter.get());
    assertEquals(eventId, firstHandledEventId.get());
    assertEquals(eventId, secondHandledEventId.get());
  }

  @Test
  void publishEvent_oneHandlerThrows_otherHandlerStillCalled() {
    var counter = new AtomicInteger(0);
    var latch = new CountDownLatch(2);
    var handledEventId = new AtomicReference<UUID>();
    var eventId = UUID.randomUUID();

    EventBus bus = new InMemoryEventBus(
        List.of(new ThrowingEventHandler(latch), new CountingEventHandler(counter, latch, handledEventId)));

    assertDoesNotThrow(() -> bus.publish(new TestEvent(eventId, "Hello world")));

    assertTrue(await(latch));
    assertEquals(1, counter.get());
    assertEquals(eventId, handledEventId.get());
  }

  @Test
  void publishEvent_wildcardHandlerRegistered_wildcardAndTypedHandlersCalled() {
    var wildcardCounter = new AtomicInteger(0);
    var typedCounter = new AtomicInteger(0);
    var latch = new CountDownLatch(2);
    var wildcardHandledEventId = new AtomicReference<UUID>();
    var typedHandledEventId = new AtomicReference<UUID>();
    var eventId = UUID.randomUUID();

    EventBus bus = new InMemoryEventBus(List.of(
        new WildcardEventHandler(wildcardCounter, latch, wildcardHandledEventId),
        new CountingEventHandler(typedCounter, latch, typedHandledEventId)));

    bus.publish(new TestEvent(eventId, "Hello world"));

    assertTrue(await(latch));
    assertEquals(1, wildcardCounter.get());
    assertEquals(1, typedCounter.get());
    assertEquals(eventId, wildcardHandledEventId.get());
    assertEquals(eventId, typedHandledEventId.get());
  }

  private record TestEvent(UUID id, String payload) implements Event.Payload {
  }

  private static class TestEventHandler implements Event.Handler<TestEvent> {

    private final AtomicReference<String> handledPayload = new AtomicReference<>();
    private final AtomicReference<UUID> handledEventId = new AtomicReference<>();
    private final CountDownLatch latch;

    private TestEventHandler(CountDownLatch latch) {
      this.latch = latch;
    }

    @Override
    public Class<TestEvent> eventType() {
      return TestEvent.class;
    }

    @Override
    public void handle(TestEvent event) {
      handledPayload.set(event.payload());
      handledEventId.set(event.id());
      latch.countDown();
    }

    AtomicReference<String> handledPayload() {
      return handledPayload;
    }

    AtomicReference<UUID> handledEventId() {
      return handledEventId;
    }
  }

  private static class CountingEventHandler implements Event.Handler<TestEvent> {

    private final AtomicInteger counter;
    private final CountDownLatch latch;
    private final AtomicReference<UUID> handledEventId;

    private CountingEventHandler(
        AtomicInteger counter,
        CountDownLatch latch,
        AtomicReference<UUID> handledEventId) {
      this.counter = counter;
      this.latch = latch;
      this.handledEventId = handledEventId;
    }

    @Override
    public Class<TestEvent> eventType() {
      return TestEvent.class;
    }

    @Override
    public void handle(TestEvent event) {
      counter.incrementAndGet();
      handledEventId.set(event.id());
      latch.countDown();
    }
  }

  private static class ThrowingEventHandler implements Event.Handler<TestEvent> {

    private final CountDownLatch latch;

    private ThrowingEventHandler(CountDownLatch latch) {
      this.latch = latch;
    }

    @Override
    public Class<TestEvent> eventType() {
      return TestEvent.class;
    }

    @Override
    public void handle(TestEvent event) {
      latch.countDown();
      throw new RuntimeException("Handler failed");
    }
  }

  private static class WildcardEventHandler implements Event.Handler<Event.Payload> {

    private final AtomicInteger counter;
    private final CountDownLatch latch;
    private final AtomicReference<UUID> handledEventId;

    private WildcardEventHandler(
        AtomicInteger counter,
        CountDownLatch latch,
        AtomicReference<UUID> handledEventId) {
      this.counter = counter;
      this.latch = latch;
      this.handledEventId = handledEventId;
    }

    @Override
    public Class<Event.Payload> eventType() {
      return null;
    }

    @Override
    public void handle(Event.Payload event) {
      counter.incrementAndGet();
      handledEventId.set(((TestEvent) event).id());
      latch.countDown();
    }
  }

  private boolean await(CountDownLatch latch) {
    try {
      return latch.await(1, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }
}
