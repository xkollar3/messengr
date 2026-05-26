package io.github.xkollar3.messengr.messengr_outbox;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxRepository extends JpaRepository<EventOutboxEntity, UUID> {

  List<EventOutboxEntity> findAllByOrderByIdAsc();
}
