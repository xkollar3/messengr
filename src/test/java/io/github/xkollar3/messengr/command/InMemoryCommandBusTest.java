package io.github.xkollar3.messengr.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

public class InMemoryCommandBusTest {

  @Test
  void commandBusConstruction_validCommandBus_busConstructed() {
    var handler = new TestCommandHandler();

    new InMemoryCommandBus(List.of(handler));

    assertTrue(true);
  }

  @Test
  void commandBusConstruction_duplicateHandlersForTestCommand_busCannotBeConstructed() {
    var handler1 = new TestCommandHandler();
    var handler2 = new TestCommandHandler();

    assertThrowsExactly(MultipleHandlersForCommand.class, () -> new InMemoryCommandBus(List.of(handler1, handler2)),
        () -> new MultipleHandlersForCommand(TestCommand.class, List.of(handler1, handler2)).getMessage());
  }

  @Test
  void invokeCommand_handlerFound_invocationSuccessful() {
    CommandBus bus = new InMemoryCommandBus(List.of(new TestCommandHandler()));

    String result = bus.invoke(new TestCommand("Hello world"));

    assertEquals("Hello world", result);
  }

  @Test
  void invokeCommand_noHandler_invocationFailed() {
    CommandBus emptyBus = new InMemoryCommandBus(List.of());

    var cmd = new TestCommand("Hello world");
    assertThrowsExactly(CommandHandlerNotFound.class, () -> emptyBus.invoke(cmd),
        () -> new CommandHandlerNotFound(cmd.getClass()).getMessage());
  }

  private record TestCommand(String payload) implements Command.Payload {
  }

  private class TestCommandHandler implements Command.Handler<TestCommand, String> {

    @Override
    public Class<TestCommand> commandType() {
      return TestCommand.class;
    }

    @Override
    public String execute(TestCommand command) {
      return command.payload();
    }
  }
}
