package com.plazoleta.pedido.domain.spi;

import java.util.List;
import java.util.Optional;

import com.plazoleta.pedido.domain.model.EstadoPedido;
import com.plazoleta.pedido.domain.model.HojaPedidos;
import com.plazoleta.pedido.domain.model.Pedido;

/**
 * Puerto de salida para la persistencia de pedidos.
 */
public interface RepositorioPedidosPuerto {

    Pedido guardar(Pedido pedido);

    Optional<Pedido> obtenerPorId(Long id);

    /**
     * ¿Tiene el cliente un pedido en proceso (pendiente, en preparación o
     * listo)? Es la regla que impide abrir pedidos en paralelo (HU11).
     */
    boolean existePedidoActivoDe(Long idCliente);

    /**
     * Pedidos de un restaurante en un estado, por hojas (HU12).
     */
    HojaPedidos buscarPorRestauranteYEstado(Long idRestaurante, EstadoPedido estado,
                                            int pagina, int cantidad);

    /**
     * Pedidos entregados de un restaurante: la materia prima de las métricas
     * de eficiencia (HU18).
     */
    List<Pedido> buscarEntregadosDe(Long idRestaurante);
}
