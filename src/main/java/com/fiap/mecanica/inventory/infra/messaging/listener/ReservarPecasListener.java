package com.fiap.mecanica.inventory.infra.messaging.listener;

import com.fiap.mecanica.inventory.application.messaging.FalhaNaReservaEvent;
import com.fiap.mecanica.inventory.application.messaging.PecasReservadasEvent;
import com.fiap.mecanica.inventory.application.messaging.ReservarPecasCommand;
import com.fiap.mecanica.inventory.domain.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.inventory.domain.exception.ItemNaoEncontradoException;
import com.fiap.mecanica.inventory.domain.service.EstoqueService;
import com.fiap.mecanica.inventory.infra.messaging.config.RabbitMqConfig;
import com.fiap.mecanica.inventory.infra.messaging.publisher.InventoryEventPublisher;
import com.fiap.mecanica.inventory.infra.persistence.entity.ProcessedCommandEntity;
import com.fiap.mecanica.inventory.infra.persistence.repository.ProcessedCommandJpaRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservarPecasListener {

  private final EstoqueService estoqueService;
  private final InventoryEventPublisher publisher;
  private final ProcessedCommandJpaRepository processedCommandRepo;

  @RabbitListener(queues = RabbitMqConfig.QUEUE_RESERVAR_PECAS)
  @Transactional
  public void onReservarPecas(ReservarPecasCommand command) {
    log.info("[MQ] Recebido ReservarPecasCommand sagaId={} osId={}", command.sagaId(),
        command.osId());

    if (processedCommandRepo.existsBySagaId(command.sagaId())) {
      log.warn("[MQ] Comando duplicado ignorado sagaId={}", command.sagaId());
      return;
    }

    processedCommandRepo.save(ProcessedCommandEntity.builder()
        .id(UUID.randomUUID())
        .sagaId(command.sagaId())
        .processedAt(LocalDateTime.now())
        .build());

    try {
      for (ReservarPecasCommand.ItemReserva item : command.itens()) {
        if ("PECA".equals(item.tipo()) || "INSUMO".equals(item.tipo())) {
          estoqueService.baixarEstoque(item.referenciaId(), item.tipo(), item.quantidade());
        }
      }
      publisher.publicarSucesso(new PecasReservadasEvent(command.sagaId(), command.osId()));
    } catch (EstoqueInsuficienteException | ItemNaoEncontradoException e) {
      log.warn("[SAGA] Estoque insuficiente sagaId={}: {}", command.sagaId(), e.getMessage());
      publisher.publicarFalha(new FalhaNaReservaEvent(command.sagaId(), command.osId(),
          e.getMessage()));
    } catch (Exception e) {
      log.error("[SAGA] Erro inesperado sagaId={}: {}", command.sagaId(), e.getMessage(), e);
      publisher.publicarFalha(new FalhaNaReservaEvent(command.sagaId(), command.osId(),
          "Erro interno no inventory-service: " + e.getMessage()));
    }
  }
}
