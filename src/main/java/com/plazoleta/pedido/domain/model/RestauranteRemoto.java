package com.plazoleta.pedido.domain.model;

/**
 * Vista de un restaurante que entrega el microservicio de restaurantes;
 * el dueño registrado se compara con quien pide las métricas (HU18).
 */
public record RestauranteRemoto(
        Long id,
        Long idPropietario) {
}
