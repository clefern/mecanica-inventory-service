package com.fiap.mecanica.inventory.infra.messaging.publisher;

import com.fiap.mecanica.inventory.application.messaging.FalhaNaReservaEvent;
import com.fiap.mecanica.inventory.application.messaging.PecasReservadasEvent;
import com.fiap.mecanica.inventory.infra.messaging.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventPublisher {

  private final RabbitTemplate rabbitTemplate;

  public void publicarSucesso(PecasReservadasEvent event) {
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.EXCHANGE, RabbitMqConfig.RK_PECAS_RESERVADAS, event);
    log.info("[MQ] PecasReservadasEvent publicado sagaId={}", event.sagaId());
  }

  public void publicarFalha(FalhaNaReservaEvent event) {
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.EXCHANGE, RabbitMqConfig.RK_FALHA_RESERVA, event);
    log.warn("[MQ] FalhaNaReservaEvent publicado sagaId={} motivo={}", event.sagaId(),
        event.motivo());
  }
}
