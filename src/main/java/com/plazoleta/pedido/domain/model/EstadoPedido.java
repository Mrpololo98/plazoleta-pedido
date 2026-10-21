package com.plazoleta.pedido.domain.model;

/**
 * Estados por los que pasa un pedido de la plazoleta (HU11 a HU16).
 *
 * PENDIENTE     → recién recibido, aún puede cancelarse (HU16)
 * EN_PREPARACION→ un empleado se lo asignó (HU13)
 * LISTO         → notificado al cliente con pin de seguridad (HU14)
 * ENTREGADO     → pin verificado, ciclo cerrado (HU15)
 * CANCELADO     → cancelado por el cliente estando pendiente (HU16)
 */
public enum EstadoPedido {

    PENDIENTE,
    EN_PREPARACION,
    LISTO,
    ENTREGADO,
    CANCELADO;

    /**
     * Estados que mantienen el pedido "en proceso": mientras exista uno de
     * ellos, el cliente no puede abrir un pedido nuevo (HU11).
     */
    public static final EstadoPedido[] EN_PROCESO = {
            PENDIENTE, EN_PREPARACION, LISTO
    };
}
