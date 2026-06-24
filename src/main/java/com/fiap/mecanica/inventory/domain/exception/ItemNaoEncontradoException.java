package com.fiap.mecanica.inventory.domain.exception;

import java.util.UUID;

public class ItemNaoEncontradoException extends RuntimeException {

  public ItemNaoEncontradoException(UUID id) {
    super("Item de estoque não encontrado: " + id);
  }
}
