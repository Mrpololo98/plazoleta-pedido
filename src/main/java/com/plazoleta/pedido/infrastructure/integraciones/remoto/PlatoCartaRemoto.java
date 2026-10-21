package com.plazoleta.pedido.infrastructure.integraciones.remoto;

/**
 * Respuesta del microservicio de restaurantes al consultar un plato: el
 * pedido valida el restaurante de origen y la disponibilidad en la carta.
 */
public record PlatoCartaRemoto(
        Long id,
        Long idRestaurante,
        Boolean activo) {
}
