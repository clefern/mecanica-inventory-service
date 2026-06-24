package com.fiap.mecanica.inventory.presentation.controller;

import com.fiap.mecanica.inventory.infra.persistence.entity.ItemComercialEntity;
import com.fiap.mecanica.inventory.infra.persistence.repository.ItemComercialJpaRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estoque")
@RequiredArgsConstructor
public class EstoqueController {

  private final ItemComercialJpaRepository itemRepository;

  @GetMapping
  public ResponseEntity<List<ItemComercialEntity>> listar() {
    return ResponseEntity.ok(itemRepository.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ItemComercialEntity> buscar(@PathVariable UUID id) {
    return itemRepository.findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
