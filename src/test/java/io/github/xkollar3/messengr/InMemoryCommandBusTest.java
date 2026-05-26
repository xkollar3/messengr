package io.github.xkollar3.messengr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.xkollar3.messengr.exception.CommandHandlerNotFound;

public class InMemoryCommandBusTest {

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

  private record TestCommand(String payload) {
  }

  private class TestCommandHandler implements CommandHandler<TestCommand, String> {

    @Override
    public Class<TestCommand> commandType() {
      return TestCommand.class;
    }

    @Override
    public String handle(TestCommand command) {
      return command.payload();
    }
  }
}
