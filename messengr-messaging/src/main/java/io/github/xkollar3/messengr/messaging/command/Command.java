package io.github.xkollar3.messengr.messaging.command;

import java.util.UUID;

public interface Command {

  public interface Handler<C extends Payload, R> {

    Class<C> commandType();

    R execute(C command);
  }

  public interface Payload {

    UUID id();

  }

  public interface Interceptor {

    <C extends Payload> C intercept(C command);
  }
}
