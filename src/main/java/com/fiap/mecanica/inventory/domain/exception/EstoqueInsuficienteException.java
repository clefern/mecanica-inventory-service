package com.fiap.mecanica.inventory.domain.exception;

import java.util.UUID;

public class EstoqueInsuficienteException extends RuntimeException {

  public EstoqueInsuficienteException(UUID referenciaId, int solicitado, int disponivel) {
    super("Estoque insuficiente para item %s: solicitado=%d disponível=%d"
        .formatted(referenciaId, solicitado, disponivel));
  }
}
