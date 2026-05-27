package io.github.xkollar3.messengr.messaging.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import io.github.xkollar3.messengr.messaging.command.Command.Payload;

public class InMemoryCommandBusTest {

  @Test
  void commandBusConstruction_validCommandBus_busConstructed() {
    var handler = new TestCommandHandler();

    new InMemoryCommandBus(List.of(handler), List.of());

    assertTrue(true);
  }

  @Test
  void commandBusConstruction_duplicateHandlersForTestCommand_busCannotBeConstructed() {
    var handler1 = new TestCommandHandler();
    var handler2 = new TestCommandHandler();

    assertThrowsExactly(MultipleHandlersForCommand.class,
        () -> new InMemoryCommandBus(List.of(handler1, handler2), List.of()),
        () -> new MultipleHandlersForCommand(TestCommand.class, List.of(handler1, handler2)).getMessage());
  }

  @Test
  void invokeCommand_handlerFound_invocationSuccessful() {
    var handler = new TestCommandHandler();
    CommandBus bus = new InMemoryCommandBus(List.of(handler), List.of());
    var commandId = UUID.randomUUID();

    String result = bus.invoke(new TestCommand(commandId, "Hello world"));

    assertEquals("Hello world", result);
    assertEquals(commandId, handler.executedCommandId().get());
  }

  @Test
  void invokeCommand_noHandler_invocationFailed() {
    CommandBus emptyBus = new InMemoryCommandBus(List.of(), List.of());

    var cmd = new TestCommand(UUID.randomUUID(), "Hello world");
    assertThrowsExactly(CommandHandlerNotFound.class, () -> emptyBus.invoke(cmd),
        () -> new CommandHandlerNotFound(cmd.getClass()).getMessage());
  }

  @Test
  void invokeCommand_interceptorInterruptsCommand_handlerIsNotCalled() {
    var handler = new TestCommandHandler();
    CommandBus bus = new InMemoryCommandBus(List.of(handler), List.of(new CancelCommandInterceptor()));

    var cmd = new TestCommand(UUID.randomUUID(), "Hello world");
    assertThrowsExactly(RuntimeException.class, () -> bus.invoke(cmd),
        () -> new RuntimeException("Command execution interrupted").getMessage());
    assertNull(handler.executedCommandId().get());
  }

  @Test
  void invokeCommand_interceptorDoesNothing_handlerIsCalled() {
    var handler = new TestCommandHandler();
    CommandBus bus = new InMemoryCommandBus(List.of(handler), List.of(new NoOpCommandInterceptor()));
    var commandId = UUID.randomUUID();

    String result = bus.invoke(new TestCommand(commandId, "Hello world"));

    assertEquals("Hello world", result);
    assertEquals(commandId, handler.executedCommandId().get());
  }

  private class CancelCommandInterceptor implements Command.Interceptor {

    @Override
    public <C extends Payload> C intercept(C command) {
      throw new RuntimeException("Command execution interrupted");
    }
  }

  private class NoOpCommandInterceptor implements Command.Interceptor {
    @Override
    public <C extends Payload> C intercept(C command) {
      return command;
    }
  }

  private record TestCommand(UUID id, String payload) implements Command.Payload {
  }

  private class TestCommandHandler implements Command.Handler<TestCommand, String> {

    private final AtomicReference<UUID> executedCommandId = new AtomicReference<>();

    @Override
    public Class<TestCommand> commandType() {
      return TestCommand.class;
    }

    @Override
    public String execute(TestCommand command) {
      executedCommandId.set(command.id());
      return command.payload();
    }

    AtomicReference<UUID> executedCommandId() {
      return executedCommandId;
    }
  }
}
