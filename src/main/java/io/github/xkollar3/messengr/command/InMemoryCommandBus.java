package io.github.xkollar3.messengr.command;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InMemoryCommandBus implements CommandBus {

  private final Map<Class<? extends Command.Payload>, Command.Handler<? extends Command.Payload, ?>> handlers;

  public InMemoryCommandBus(List<Command.Handler<? extends Command.Payload, ?>> handlers) {
    validateNoDuplicitHandlers(handlers);

    this.handlers = handlers.stream().collect(Collectors.toMap(Command.Handler::commandType, Function.identity()));
  }

  @Override
  public <C extends Command.Payload, R> R invoke(C command) {
    Class<?> commandClass = command.getClass();

    @SuppressWarnings("unchecked")
    Command.Handler<C, R> handler = (Command.Handler<C, R>) handlers.get(commandClass);

    if (handler == null) {
      throw new CommandHandlerNotFound(commandClass);
    }

    return handler.execute(command);
  }

  private void validateNoDuplicitHandlers(List<Command.Handler<? extends Command.Payload, ?>> handlers) {
    var commands = handlers.stream().map(Command.Handler::commandType);

    var duplicate = commands
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet()
        .stream()
        .filter(e -> e.getValue() > 1)
        .findFirst();

    if (duplicate.isPresent()) {
      var violators = handlers.stream().filter(handler -> handler.commandType().equals(duplicate.get().getKey()))
          .toList();
      throw new MultipleHandlersForCommand(duplicate.get().getKey(), violators);
    }
  }
}
