package com.plazoleta.pedido.infrastructure.integraciones.remoto;

/**
 * Respuesta del microservicio de restaurantes al consultar un
 * establecimiento: el dueño registrado decide quién ve las métricas.
 */
public record RestauranteRemotoRespuesta(
        Long id,
        Long idPropietario) {
}
