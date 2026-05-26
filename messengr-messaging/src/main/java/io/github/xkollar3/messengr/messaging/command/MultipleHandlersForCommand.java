package io.github.xkollar3.messengr.messaging.command;

import java.util.List;
import java.util.stream.Collectors;

public class MultipleHandlersForCommand extends RuntimeException {

  private final Class<? extends Command.Payload> payloadClass;

  private final List<Command.Handler<? extends Command.Payload, ?>> violators;

  public MultipleHandlersForCommand(Class<? extends Command.Payload> commandPayload,
      List<Command.Handler<? extends Command.Payload, ?>> handlers) {
    super();
    this.payloadClass = commandPayload;
    this.violators = handlers;
  }

  @Override
  public String getMessage() {
    return "Found multiple handlers for: " + payloadClass.getSimpleName() + ",["
        + violators.stream().map(violator -> violator.getClass().getSimpleName()).collect(Collectors.joining(", "))
        + "]";
  }
}
