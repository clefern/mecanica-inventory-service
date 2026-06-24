package com.fiap.mecanica.inventory.infra.persistence.repository;

import com.fiap.mecanica.inventory.infra.persistence.entity.ProcessedCommandEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedCommandJpaRepository extends JpaRepository<ProcessedCommandEntity, UUID> {
  boolean existsBySagaId(UUID sagaId);
}
