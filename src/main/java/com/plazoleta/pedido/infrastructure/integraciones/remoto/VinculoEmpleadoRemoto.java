package com.plazoleta.pedido.infrastructure.integraciones.remoto;

/**
 * Respuesta del microservicio de restaurantes al consultar el vínculo de
 * un empleado: su restaurante de trabajo.
 */
public record VinculoEmpleadoRemoto(
        Long id,
        Long idUsuario,
        Long idRestaurante) {
}
