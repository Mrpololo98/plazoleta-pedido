package com.plazoleta.pedido.domain.model;

import java.time.Instant;

/**
 * Un cambio de estado de un pedido, tal como lo exige la HU17: queda un
 * registro por cada transición y el cliente puede consultar el historial
 * completo de sus propios pedidos.
 */
public record RegistroTrazabilidad(
        Long id,
        Long idPedido,
        EstadoPedido estadoAnterior,
        EstadoPedido estadoNuevo,
        Instant fecha) {
}
