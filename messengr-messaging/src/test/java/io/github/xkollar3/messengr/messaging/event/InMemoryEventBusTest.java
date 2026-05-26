package io.github.xkollar3.messengr.messaging.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
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

    bus.publish(new TestEvent("Hello world"));

    assertTrue(await(latch));
    assertEquals("Hello world", handler.handledPayload().get());
  }

  @Test
  void publishEvent_noHandler_publishSuccessful() {
    EventBus emptyBus = new InMemoryEventBus(List.of());

    var event = new TestEvent("Hello world");
    assertDoesNotThrow(() -> emptyBus.publish(event));
  }

  @Test
  void publishEvent_twoHandlersForEvent_bothHandlersCalled() {
    var counter = new AtomicInteger(0);
    var latch = new CountDownLatch(2);

    EventBus bus = new InMemoryEventBus(List.of(new CountingEventHandler(counter, latch), new CountingEventHandler(counter, latch)));

    bus.publish(new TestEvent("Hello world"));

    assertTrue(await(latch));
    assertEquals(2, counter.get());
  }

  @Test
  void publishEvent_oneHandlerThrows_otherHandlerStillCalled() {
    var counter = new AtomicInteger(0);
    var latch = new CountDownLatch(2);

    EventBus bus = new InMemoryEventBus(List.of(new ThrowingEventHandler(latch), new CountingEventHandler(counter, latch)));

    assertDoesNotThrow(() -> bus.publish(new TestEvent("Hello world")));

    assertTrue(await(latch));
    assertEquals(1, counter.get());
  }

  @Test
  void publishEvent_wildcardHandlerRegistered_wildcardAndTypedHandlersCalled() {
    var wildcardCounter = new AtomicInteger(0);
    var typedCounter = new AtomicInteger(0);
    var latch = new CountDownLatch(2);

    EventBus bus = new InMemoryEventBus(List.of(
        new WildcardEventHandler(wildcardCounter, latch),
        new CountingEventHandler(typedCounter, latch)));

    bus.publish(new TestEvent("Hello world"));

    assertTrue(await(latch));
    assertEquals(1, wildcardCounter.get());
    assertEquals(1, typedCounter.get());
  }

  private record TestEvent(String payload) implements Event.Payload {
  }

  private static class TestEventHandler implements Event.Handler<TestEvent> {

    private final AtomicReference<String> handledPayload = new AtomicReference<>();
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
      latch.countDown();
    }

    AtomicReference<String> handledPayload() {
      return handledPayload;
    }
  }

  private static class CountingEventHandler implements Event.Handler<TestEvent> {

    private final AtomicInteger counter;
    private final CountDownLatch latch;

    private CountingEventHandler(AtomicInteger counter, CountDownLatch latch) {
      this.counter = counter;
      this.latch = latch;
    }

    @Override
    public Class<TestEvent> eventType() {
      return TestEvent.class;
    }

    @Override
    public void handle(TestEvent event) {
      counter.incrementAndGet();
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

    private WildcardEventHandler(AtomicInteger counter, CountDownLatch latch) {
      this.counter = counter;
      this.latch = latch;
    }

    @Override
    public Class<Event.Payload> eventType() {
      return null;
    }

    @Override
    public void handle(Event.Payload event) {
      counter.incrementAndGet();
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
