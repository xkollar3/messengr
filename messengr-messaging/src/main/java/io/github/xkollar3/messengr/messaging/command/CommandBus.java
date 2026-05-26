package io.github.xkollar3.messengr.messaging.command;

public interface CommandBus {

  public <C extends Command.Payload, R> R invoke(C command);
}
