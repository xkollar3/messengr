package io.github.xkollar3.messengr;

public interface CommandBus {

  public <C, R> R invoke(C command);
}
