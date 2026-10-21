package com.plazoleta.pedido.domain.api;

import java.util.List;

import com.plazoleta.pedido.domain.model.LineaPedido;
import com.plazoleta.pedido.domain.model.Pedido;

/**
 * Caso de uso HU11: realizar pedido.
 *
 * Un pedido nace pendiente, solo puede llevar platos de un mismo
 * restaurante (todos habilitados) y el cliente no puede abrir uno nuevo
 * mientras tenga otro en proceso.
 */
public interface CrearPedido {

    Pedido ejecutar(Long idCliente, Long idRestaurante, List<LineaPedido> lineas);
}
