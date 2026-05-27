package io.github.xkollar3.messengr.messengr_outbox;

import io.github.xkollar3.messengr.messaging.event.Event;

/**
 * Infrastructure-agnostic contract for relaying outbox messages.
 */
public interface MessageRelayPort {

  boolean relay(Event.Payload event);
}
