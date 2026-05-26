package io.github.xkollar3.messengr;

public interface CommandHandler<C, R> {

  Class<C> commandType();

  R handle(C command);

}
