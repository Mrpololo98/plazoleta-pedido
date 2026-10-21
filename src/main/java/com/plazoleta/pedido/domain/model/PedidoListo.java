package com.plazoleta.pedido.domain.model;

/**
 * Resultado de marcar un pedido como listo (HU14): el pedido actualizado,
 * el pin de seguridad notificado al cliente (viaja solo por SMS; se expone
 * en la respuesta para poder ejercitar la entrega en el entorno de demo) y
 * el teléfono al que se envió la notificación simulada.
 */
public record PedidoListo(
        Pedido pedido,
        String pinSeguridad,
        String telefonoNotificado) {
}
