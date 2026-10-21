package com.plazoleta.pedido.domain.model;

import java.util.List;

/**
 * Reporte de eficiencia del restaurante (HU18): la duración de cada pedido
 * entregado y el ranking de empleados por tiempo promedio de atención.
 */
public record ReporteEficiencia(
        Long idRestaurante,
        List<ResumenPedido> pedidos,
        List<PuestoEmpleado> rankingEmpleados) {

    /**
     * Un pedido terminado con su tiempo total entre inicio y fin.
     */
    public record ResumenPedido(
            Long idPedido,
            Long idCliente,
            Long idEmpleado,
            EstadoPedido estado,
            long duracionMinutos) {
    }

    /**
     * Un empleado del ranking: cuántos pedidos entregó y su promedio.
     */
    public record PuestoEmpleado(
            Long idEmpleado,
            int pedidosEntregados,
            double promedioMinutos) {
    }
}
