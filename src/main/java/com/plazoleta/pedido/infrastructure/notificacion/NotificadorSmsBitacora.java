package com.plazoleta.pedido.infrastructure.notificacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.plazoleta.pedido.domain.spi.NotificadorSmsPuerto;

/**
 * Notificador SMS del entorno de demo (HU14): deja el mensaje en la
 * bitácora del microservicio con el mismo formato que enviaría el proveedor
 * real (Twilio, SNS u otro).
 *
 * Cambiar este adaptador por uno de proveedor no toca el dominio ni los
 * casos de uso: ese es el punto del puerto.
 */
@Component
public class NotificadorSmsBitacora implements NotificadorSmsPuerto {

    private static final Logger BITACORA = LoggerFactory.getLogger(NotificadorSmsBitacora.class);

    @Override
    public void notificarPedidoListo(String telefono, Long idPedido, String pinSeguridad) {
        BITACORA.info("[SMS SIMULADO] Para {} -> Tu pedido {} de la plazoleta está listo. "
                        + "PIN de seguridad para reclamarlo: {}",
                telefono, idPedido, pinSeguridad);
    }
}
