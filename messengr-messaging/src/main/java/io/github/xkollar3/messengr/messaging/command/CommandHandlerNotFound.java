package io.github.xkollar3.messengr.messaging.command;

public class CommandHandlerNotFound extends RuntimeException {

  private final Class<?> commandClass;

  public CommandHandlerNotFound(Class<?> commandClass) {
    super();
    this.commandClass = commandClass;
  }

  @Override
  public String getMessage() {
    return "No handler found for command of class: " + commandClass.getSimpleName();
  }
}
