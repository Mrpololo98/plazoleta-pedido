package com.plazoleta.pedido.domain.api;

import com.plazoleta.pedido.domain.model.Pedido;

/**
 * Caso de uso HU16: cancelar pedido.
 *
 * El cliente solo puede cancelar sus propios pedidos y únicamente si aún
 * están pendientes.
 */
public interface CancelarPedido {

    Pedido ejecutar(Long idPedido, Long idCliente);
}
