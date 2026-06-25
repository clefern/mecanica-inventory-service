package com.fiap.mecanica.inventory.infra.messaging.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.inventory.application.messaging.FalhaNaReservaEvent;
import com.fiap.mecanica.inventory.application.messaging.PecasReservadasEvent;
import com.fiap.mecanica.inventory.application.messaging.ReservarPecasCommand;
import com.fiap.mecanica.inventory.application.messaging.ReservarPecasCommand.ItemReserva;
import com.fiap.mecanica.inventory.domain.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.inventory.domain.exception.ItemNaoEncontradoException;
import com.fiap.mecanica.inventory.domain.service.EstoqueService;
import com.fiap.mecanica.inventory.infra.messaging.publisher.InventoryEventPublisher;
import com.fiap.mecanica.inventory.infra.persistence.repository.ProcessedCommandJpaRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservarPecasListenerTest {

  @Mock EstoqueService estoqueService;
  @Mock InventoryEventPublisher publisher;
  @Mock ProcessedCommandJpaRepository processedCommandRepo;

  @InjectMocks ReservarPecasListener listener;

  @Test
  void onReservarPecas_novoComando_deveProcessarEPublicarSucesso() {
    UUID sagaId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    UUID pecaId = UUID.randomUUID();
    ReservarPecasCommand command = new ReservarPecasCommand(sagaId, osId,
        List.of(new ItemReserva(pecaId, "PECA", 2)));

    when(processedCommandRepo.existsBySagaId(sagaId)).thenReturn(false);
    when(processedCommandRepo.save(any())).thenAnswer(i -> i.getArgument(0));

    listener.onReservarPecas(command);

    verify(estoqueService).baixarEstoque(pecaId, "PECA", 2);
    verify(publisher).publicarSucesso(argThat(e ->
        e.sagaId().equals(sagaId) && e.osId().equals(osId)));
    verify(publisher, never()).publicarFalha(any());
  }

  @Test
  void onReservarPecas_comandoDuplicado_deveIgnorar() {
    UUID sagaId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    ReservarPecasCommand command = new ReservarPecasCommand(sagaId, osId, List.of());

    when(processedCommandRepo.existsBySagaId(sagaId)).thenReturn(true);

    listener.onReservarPecas(command);

    verify(estoqueService, never()).baixarEstoque(any(), any(), any(int.class));
    verify(publisher, never()).publicarSucesso(any());
    verify(publisher, never()).publicarFalha(any());
  }

  @Test
  void onReservarPecas_estoqueInsuficiente_devePublicarFalha() {
    UUID sagaId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    UUID pecaId = UUID.randomUUID();
    ReservarPecasCommand command = new ReservarPecasCommand(sagaId, osId,
        List.of(new ItemReserva(pecaId, "PECA", 5)));

    when(processedCommandRepo.existsBySagaId(sagaId)).thenReturn(false);
    when(processedCommandRepo.save(any())).thenAnswer(i -> i.getArgument(0));
    doThrow(new EstoqueInsuficienteException(pecaId, 5, 2))
        .when(estoqueService).baixarEstoque(pecaId, "PECA", 5);

    listener.onReservarPecas(command);

    verify(publisher, never()).publicarSucesso(any());
    verify(publisher).publicarFalha(argThat(e -> e.sagaId().equals(sagaId)));
  }

  @Test
  void onReservarPecas_itemNaoEncontrado_devePublicarFalha() {
    UUID sagaId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    UUID pecaId = UUID.randomUUID();
    ReservarPecasCommand command = new ReservarPecasCommand(sagaId, osId,
        List.of(new ItemReserva(pecaId, "INSUMO", 1)));

    when(processedCommandRepo.existsBySagaId(sagaId)).thenReturn(false);
    when(processedCommandRepo.save(any())).thenAnswer(i -> i.getArgument(0));
    doThrow(new ItemNaoEncontradoException(pecaId))
        .when(estoqueService).baixarEstoque(pecaId, "INSUMO", 1);

    listener.onReservarPecas(command);

    verify(publisher).publicarFalha(argThat(e -> e.sagaId().equals(sagaId)));
  }

  @Test
  void onReservarPecas_erroGenerico_devePublicarFalhaComErroInterno() {
    UUID sagaId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    UUID pecaId = UUID.randomUUID();
    ReservarPecasCommand command = new ReservarPecasCommand(sagaId, osId,
        List.of(new ItemReserva(pecaId, "PECA", 1)));

    when(processedCommandRepo.existsBySagaId(sagaId)).thenReturn(false);
    when(processedCommandRepo.save(any())).thenAnswer(i -> i.getArgument(0));
    doThrow(new RuntimeException("erro de banco"))
        .when(estoqueService).baixarEstoque(any(), any(), any(int.class));

    listener.onReservarPecas(command);

    verify(publisher).publicarFalha(argThat(e ->
        e.sagaId().equals(sagaId) && e.motivo().contains("Erro interno")));
  }
}
