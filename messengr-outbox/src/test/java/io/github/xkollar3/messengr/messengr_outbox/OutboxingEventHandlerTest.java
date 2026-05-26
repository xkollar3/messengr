package io.github.xkollar3.messengr.messengr_outbox;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.xkollar3.messengr.messaging.event.Event;

@SpringJUnitConfig(classes = OutboxingEventHandlerTest.TestConfig.class)
class OutboxingEventHandlerTest {

  @Autowired
  private OutboxRepository outboxRepository;

  @Test
  void handle_persistsEventPayloadToOutboxTable() {
    var handler = new OutboxingEventHandler(outboxRepository, new ObjectMapper());

    handler.handle(new TestEvent("hello-world"));

    var outboxRows = outboxRepository.findAll();

    assertEquals(1, outboxRows.size());
    assertEquals("{\"message\":\"hello-world\"}", outboxRows.getFirst().getEventData());
  }

  private record TestEvent(String message) implements Event.Payload {
  }

  @Configuration
  @EnableAutoConfiguration
  @ImportAutoConfiguration({ DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
  @EntityScan(basePackageClasses = EventOutboxEntity.class)
  @EnableJpaRepositories(basePackageClasses = OutboxRepository.class)
  static class TestConfig {

  }
}
