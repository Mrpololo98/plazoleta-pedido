package com.plazoleta.pedido.domain.api;

import com.plazoleta.pedido.domain.model.EstadoPedido;
import com.plazoleta.pedido.domain.model.HojaPedidos;

/**
 * Caso de uso HU12: obtener la lista de pedidos filtrando por estado.
 *
 * El empleado solo ve los pedidos del restaurante al que pertenece y la
 * hoja se pagina según la cantidad que él pida.
 */
public interface ListarPedidos {

    HojaPedidos ejecutar(Long idEmpleado, EstadoPedido estado, int pagina, int cantidad);
}
