package io.github.xkollar3.messengr.messaging.command;

public interface Command {

  public interface Handler<C extends Payload, R> {

    Class<C> commandType();

    R execute(C command);
  }

  public interface Payload {

  }

  public interface Interceptor {

    <C extends Payload> C intercept(C command);
  }
}
