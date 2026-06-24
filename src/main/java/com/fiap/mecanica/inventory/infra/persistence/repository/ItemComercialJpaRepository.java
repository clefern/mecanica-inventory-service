package com.fiap.mecanica.inventory.infra.persistence.repository;

import com.fiap.mecanica.inventory.infra.persistence.entity.ItemComercialEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemComercialJpaRepository extends JpaRepository<ItemComercialEntity, UUID> {}
