package com.plazoleta.pedido.application.dto;

import java.time.Instant;
import java.util.List;

import com.plazoleta.pedido.domain.model.EstadoPedido;

/**
 * Vista de un pedido en cualquiera de sus estados (HU11 a HU16).
 * Incluye las líneas con su cantidad y el empleado responsable, si ya
 * tiene uno asignado.
 */
public record RespuestaPedido(
        Long id,
        Long idCliente,
        Long idRestaurante,
        EstadoPedido estado,
        List<LineaRespuesta> platos,
        Long idEmpleadoAsignado,
        Instant fechaCreacion,
        Instant fechaCierre) {

    /**
     * Un plato del pedido con su cantidad.
     */
    public record LineaRespuesta(
            Long idPlato,
            int cantidad) {
    }
}
