package io.github.xkollar3.messengr.messengr_outbox;

import java.util.List;

import io.github.xkollar3.messengr.messaging.event.Event;

public interface MessageOutboxPort {

  void outbox(Event.Payload payload);

  List<Event.Payload> getEventsToRelay(Long amount);

  void markEventsAsRelayed(List<Event.Payload> events);

}
