package com.plazoleta.pedido.domain.api;

import java.util.List;

import com.plazoleta.pedido.domain.model.RegistroTrazabilidad;

/**
 * Caso de uso HU17: consultar la trazabilidad del pedido.
 *
 * Devuelve el historial de cambios de estado; cada cliente únicamente
 * puede ver el historial de sus propios pedidos.
 */
public interface ConsultarTrazabilidad {

    List<RegistroTrazabilidad> ejecutar(Long idPedido, Long idCliente);
}
