package io.github.xkollar3.messengr.command;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.github.xkollar3.messengr.exception.CommandHandlerNotFound;

public class InMemoryCommandBus implements CommandBus {

  private final Map<Class<?>, Command.Handler<? extends Command.Payload, ?>> handlers;

  public InMemoryCommandBus(List<Command.Handler<? extends Command.Payload, ?>> handlers) {
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

    return handler.handle(command);
  }
}
