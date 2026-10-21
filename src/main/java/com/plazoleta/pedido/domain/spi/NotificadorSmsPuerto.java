package com.plazoleta.pedido.domain.spi;

/**
 * Puerto de salida hacia el servicio de mensajería SMS (HU14).
 *
 * En producción lo implementaría un proveedor real (Twilio, SNS o similar);
 * en este reto la implementación de demo deja el mensaje en la bitácora del
 * microservicio para poder evidenciar la notificación.
 */
public interface NotificadorSmsPuerto {

    void notificarPedidoListo(String telefono, Long idPedido, String pinSeguridad);
}
