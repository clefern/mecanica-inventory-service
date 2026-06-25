package com.fiap.mecanica.inventory.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.inventory.domain.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.inventory.domain.exception.ItemNaoEncontradoException;
import com.fiap.mecanica.inventory.infra.persistence.entity.ItemComercialEntity;
import com.fiap.mecanica.inventory.infra.persistence.repository.ItemComercialJpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

  @Mock ItemComercialJpaRepository itemRepository;

  @InjectMocks EstoqueService estoqueService;

  @Test
  void baixarEstoque_peca_deveDecrementarQuantidade() {
    UUID id = UUID.randomUUID();
    ItemComercialEntity item = buildItem(id, "PECA", 10);
    when(itemRepository.findById(id)).thenReturn(Optional.of(item));

    estoqueService.baixarEstoque(id, "PECA", 3);

    ArgumentCaptor<ItemComercialEntity> captor = ArgumentCaptor.forClass(ItemComercialEntity.class);
    verify(itemRepository).save(captor.capture());
    assertThat(captor.getValue().getQuantidadeEstoque()).isEqualTo(7);
  }

  @Test
  void baixarEstoque_insumo_deveDecrementarQuantidade() {
    UUID id = UUID.randomUUID();
    ItemComercialEntity item = buildItem(id, "INSUMO", 5);
    when(itemRepository.findById(id)).thenReturn(Optional.of(item));

    estoqueService.baixarEstoque(id, "INSUMO", 2);

    ArgumentCaptor<ItemComercialEntity> captor = ArgumentCaptor.forClass(ItemComercialEntity.class);
    verify(itemRepository).save(captor.capture());
    assertThat(captor.getValue().getQuantidadeEstoque()).isEqualTo(3);
  }

  @Test
  void baixarEstoque_insuficiente_deveLancarException() {
    UUID id = UUID.randomUUID();
    ItemComercialEntity item = buildItem(id, "PECA", 2);
    when(itemRepository.findById(id)).thenReturn(Optional.of(item));

    assertThatThrownBy(() -> estoqueService.baixarEstoque(id, "PECA", 5))
        .isInstanceOf(EstoqueInsuficienteException.class);

    verify(itemRepository, never()).save(any());
  }

  @Test
  void baixarEstoque_itemNaoEncontrado_deveLancarException() {
    UUID id = UUID.randomUUID();
    when(itemRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> estoqueService.baixarEstoque(id, "PECA", 1))
        .isInstanceOf(ItemNaoEncontradoException.class);

    verify(itemRepository, never()).save(any());
  }

  @Test
  void estornarEstoque_itemEncontrado_deveIncrementarQuantidade() {
    UUID id = UUID.randomUUID();
    ItemComercialEntity item = buildItem(id, "PECA", 3);
    when(itemRepository.findById(id)).thenReturn(Optional.of(item));

    estoqueService.estornarEstoque(id, "PECA", 2);

    ArgumentCaptor<ItemComercialEntity> captor = ArgumentCaptor.forClass(ItemComercialEntity.class);
    verify(itemRepository).save(captor.capture());
    assertThat(captor.getValue().getQuantidadeEstoque()).isEqualTo(5);
  }

  @Test
  void estornarEstoque_itemNaoEncontrado_deveSerSilencioso() {
    UUID id = UUID.randomUUID();
    when(itemRepository.findById(id)).thenReturn(Optional.empty());

    estoqueService.estornarEstoque(id, "PECA", 2);

    verify(itemRepository, never()).save(any());
  }

  private ItemComercialEntity buildItem(UUID id, String tipo, int quantidade) {
    return ItemComercialEntity.builder()
        .id(id)
        .tipoItem(tipo)
        .quantidadeEstoque(quantidade)
        .build();
  }
}
