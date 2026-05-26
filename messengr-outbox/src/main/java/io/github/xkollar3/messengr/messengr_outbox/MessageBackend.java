package io.github.xkollar3.messengr.messengr_outbox;

/**
 * Infrastructure-agnostic contract for relaying outbox messages.
 */
public interface MessageBackend {

  void relay(JsonMessage message);

  record JsonMessage(String value) {
  }
}
