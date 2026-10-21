package com.plazoleta.pedido.domain.model;

import java.util.List;

/**
 * Porción de resultados del listado de pedidos por estado (HU12).
 *
 * Tipo propio del dominio: la paginación viaja por los puertos sin exponer
 * tipos de infraestructura como Page de Spring.
 */
public record HojaPedidos(
        List<Pedido> contenido,
        int pagina,
        int cantidad,
        long totalElementos) {

    /**
     * Cantidad de hojas que produce el total de pedidos con el tamaño dado.
     */
    public int totalHojas() {
        return cantidad <= 0 ? 0 : (int) Math.ceil((double) totalElementos / cantidad);
    }
}
