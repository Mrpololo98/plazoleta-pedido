package com.plazoleta.pedido.infrastructure.rest;

import java.time.Instant;

/**
 * Cuerpo de error uniforme de la API de pedidos.
 */
public record DetalleError(
        int estado,
        String mensaje,
        Instant ocurridoEn) {
}
