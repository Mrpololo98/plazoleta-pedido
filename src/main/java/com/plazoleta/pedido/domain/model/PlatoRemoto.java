package com.plazoleta.pedido.domain.model;

/**
 * Vista de la ficha de un plato que entrega el microservicio de
 * restaurantes; el pedido la usa para validar sus líneas (HU11).
 */
public record PlatoRemoto(
        Long id,
        Long idRestaurante,
        boolean activo) {
}
