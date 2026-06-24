package com.fiap.mecanica.inventory.application.messaging;

import java.util.UUID;

public record PecasReservadasEvent(UUID sagaId, UUID osId) {}
