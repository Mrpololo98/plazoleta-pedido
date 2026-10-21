package com.plazoleta.pedido.domain.api;

import com.plazoleta.pedido.domain.model.Pedido;

/**
 * Caso de uso HU15: entregar pedido.
 *
 * Solo un pedido listo puede entregarse y solo con el pin de seguridad que
 * se le envió al cliente; con la entrega el ciclo queda cerrado.
 */
public interface EntregarPedido {

    Pedido ejecutar(Long idPedido, Long idEmpleado, String pinSeguridad);
}
