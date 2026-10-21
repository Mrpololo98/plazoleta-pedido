package com.plazoleta.pedido.domain.model;

/**
 * Vínculo laboral (cuenta de empleado → restaurante) que entrega el
 * microservicio de restaurantes; con él se filtran los pedidos que un
 * empleado puede ver y tocar (HU12 a HU15).
 */
public record VinculoEmpleado(
        Long idUsuario,
        Long idRestaurante) {
}
