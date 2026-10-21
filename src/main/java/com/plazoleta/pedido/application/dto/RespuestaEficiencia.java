package com.plazoleta.pedido.application.dto;

import java.util.List;

import com.plazoleta.pedido.domain.model.EstadoPedido;

/**
 * Reporte de eficiencia de los pedidos de un restaurante (HU18): la
 * duración de cada pedido entregado y el ranking de empleados.
 */
public record RespuestaEficiencia(
        Long idRestaurante,
        List<ResumenPedido> pedidos,
        List<PuestoEmpleado> rankingEmpleados) {

    /**
     * Un pedido entregado y su duración total.
     */
    public record ResumenPedido(
            Long idPedido,
            Long idCliente,
            Long idEmpleado,
            EstadoPedido estado,
            long duracionMinutos) {
    }

    /**
     * Un empleado del ranking: pedidos entregados y tiempo promedio.
     */
    public record PuestoEmpleado(
            Long idEmpleado,
            int pedidosEntregados,
            double promedioMinutos) {
    }
}
