package com.plazoleta.pedido.domain.model;

/**
 * Cada plato escogido dentro de un pedido, con su cantidad (HU11).
 */
public record LineaPedido(
        Long idPlato,
        int cantidad) {
}
