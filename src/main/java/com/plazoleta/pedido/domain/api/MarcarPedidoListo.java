package com.plazoleta.pedido.domain.api;

import com.plazoleta.pedido.domain.model.PedidoListo;

/**
 * Caso de uso HU14: notificar que el pedido está listo.
 *
 * Genera el pin de seguridad, lo guarda cifrado y notifica al cliente por
 * SMS al teléfono que tiene registrado su cuenta.
 */
public interface MarcarPedidoListo {

    PedidoListo ejecutar(Long idPedido, Long idEmpleado);
}
