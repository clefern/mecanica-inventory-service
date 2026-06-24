package com.fiap.mecanica.inventory.application.messaging;

import java.util.UUID;

public record FalhaNaReservaEvent(UUID sagaId, UUID osId, String motivo) {}
