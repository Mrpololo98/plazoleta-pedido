package com.plazoleta.pedido.application.dto;

import com.plazoleta.pedido.domain.model.EstadoPedido;

/**
 * Vista del pedido marcado como listo (HU14): confirma el estado, el
 * teléfono notificado y el pin de seguridad.
 *
 * En producción el pin viaja únicamente por el SMS; en este entorno de
 * demo también se devuelve en la respuesta para poder ejercitar la
 * entrega (HU15) sin acceso al teléfono del cliente.
 */
public record RespuestaPedidoListo(
        Long id,
        EstadoPedido estado,
        String telefonoNotificado,
        String pinSeguridad) {
}
