package io.github.xkollar3.messengr;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.github.xkollar3.messengr.exception.CommandHandlerNotFound;

public class InMemoryCommandBus implements CommandBus {

  private final Map<Class<?>, CommandHandler<?, ?>> handlers;

  public InMemoryCommandBus(List<CommandHandler<?, ?>> handlers) {
    this.handlers = handlers.stream().collect(Collectors.toMap(CommandHandler::commandType, Function.identity()));
  }

  @Override
  public <C, R> R invoke(C command) {
    Class<?> commandClass = command.getClass();

    @SuppressWarnings("unchecked")
    CommandHandler<C, R> handler = (CommandHandler<C, R>) handlers.get(commandClass);

    if (handler == null) {
      throw new CommandHandlerNotFound(commandClass);
    }

    return handler.handle(command);
  }
}
