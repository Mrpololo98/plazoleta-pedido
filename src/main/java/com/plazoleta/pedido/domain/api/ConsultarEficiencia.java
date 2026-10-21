package com.plazoleta.pedido.domain.api;

import com.plazoleta.pedido.domain.model.ReporteEficiencia;

/**
 * Caso de uso HU18: consultar la eficiencia de los pedidos.
 *
 * Muestra de cada pedido entregado el tiempo entre su inicio y su fin, y
 * un ranking de empleados por tiempo promedio de atención. Solo el dueño
 * del restaurante puede consultarlo.
 */
public interface ConsultarEficiencia {

    ReporteEficiencia ejecutar(Long idRestaurante, Long idPropietario);
}
