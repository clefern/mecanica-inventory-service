package com.fiap.mecanica.inventory.domain.service;

import com.fiap.mecanica.inventory.domain.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.inventory.domain.exception.ItemNaoEncontradoException;
import com.fiap.mecanica.inventory.infra.persistence.entity.ItemComercialEntity;
import com.fiap.mecanica.inventory.infra.persistence.repository.ItemComercialJpaRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstoqueService {

  private final ItemComercialJpaRepository itemRepository;

  @Transactional
  public void baixarEstoque(UUID referenciaId, String tipo, int quantidade) {
    ItemComercialEntity item = itemRepository.findById(referenciaId)
        .orElseThrow(() -> new ItemNaoEncontradoException(referenciaId));

    int disponivel = item.getQuantidadeEstoque();
    if (disponivel < quantidade) {
      throw new EstoqueInsuficienteException(referenciaId, quantidade, disponivel);
    }

    item.setQuantidadeEstoque(disponivel - quantidade);
    itemRepository.save(item);
    log.info("[ESTOQUE] Baixa: item={} tipo={} qtd={} saldo={}", referenciaId, tipo, quantidade,
        item.getQuantidadeEstoque());
  }

  @Transactional
  public void estornarEstoque(UUID referenciaId, String tipo, int quantidade) {
    itemRepository.findById(referenciaId).ifPresent(item -> {
      item.setQuantidadeEstoque(item.getQuantidadeEstoque() + quantidade);
      itemRepository.save(item);
      log.info("[ESTOQUE] Estorno: item={} tipo={} qtd={} saldo={}", referenciaId, tipo, quantidade,
          item.getQuantidadeEstoque());
    });
  }
}
