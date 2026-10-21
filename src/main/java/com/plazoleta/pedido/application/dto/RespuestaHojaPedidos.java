package com.plazoleta.pedido.application.dto;

import java.util.List;

/**
 * Hoja de resultados del listado de pedidos por estado (HU12).
 */
public record RespuestaHojaPedidos(
        List<RespuestaPedido> pedidos,
        int pagina,
        int cantidad,
        long totalElementos,
        int totalHojas) {
}
