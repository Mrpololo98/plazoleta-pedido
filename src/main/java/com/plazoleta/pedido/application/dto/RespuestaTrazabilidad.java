package com.plazoleta.pedido.application.dto;

import java.time.Instant;

import com.plazoleta.pedido.domain.model.EstadoPedido;

/**
 * Un cambio de estado del historial de un pedido (HU17).
 */
public record RespuestaTrazabilidad(
        Long id,
        Long idPedido,
        EstadoPedido estadoAnterior,
        EstadoPedido estadoNuevo,
        Instant fecha) {
}
