package io.github.xkollar3.messengr.messaging.command;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.github.xkollar3.messengr.messaging.command.Command.Interceptor;

public class InMemoryCommandBus implements CommandBus {

  private final List<Interceptor> interceptors;
  private final Map<Class<? extends Command.Payload>, Command.Handler<? extends Command.Payload, ?>> handlers;

  public InMemoryCommandBus(List<Command.Handler<? extends Command.Payload, ?>> handlers,
      List<Interceptor> interceptors) {
    validateNoDuplicitHandlers(handlers);

    this.interceptors = interceptors;
    this.handlers = handlers.stream().collect(Collectors.toMap(Command.Handler::commandType, Function.identity()));
  }

  @Override
  public <C extends Command.Payload, R> R invoke(C command) {
    C interceptedCommand = applyInterceptors(command);
    Class<?> commandClass = interceptedCommand.getClass();

    @SuppressWarnings("unchecked")
    Command.Handler<C, R> handler = (Command.Handler<C, R>) handlers.get(commandClass);

    if (handler == null) {
      throw new CommandHandlerNotFound(commandClass);
    }

    return handler.execute(interceptedCommand);
  }

  private <C extends Command.Payload> C applyInterceptors(C command) {
    C current = command;

    for (Interceptor interceptor : interceptors) {
      current = interceptor.intercept(current);
    }

    return current;
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
