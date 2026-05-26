package io.github.xkollar3.messengr.command;

public interface Command {

  public interface Handler<C extends Payload, R> {

    Class<C> commandType();

    R handle(C command);
  }

  public interface Payload {

  }
}
