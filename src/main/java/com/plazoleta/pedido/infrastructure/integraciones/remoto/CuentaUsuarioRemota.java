package com.plazoleta.pedido.infrastructure.integraciones.remoto;

/**
 * Respuesta del microservicio de usuarios al consultar una cuenta: del
 * cliente solo interesa el nombre y el celular para notificarle el pin.
 */
public record CuentaUsuarioRemota(
        Long id,
        String nombre,
        String apellido,
        String celular,
        String correo,
        String rol) {
}
