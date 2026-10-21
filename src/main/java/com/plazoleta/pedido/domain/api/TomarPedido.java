package com.plazoleta.pedido.domain.api;

import com.plazoleta.pedido.domain.model.Pedido;

/**
 * Caso de uso HU13: asignarse a un pedido y cambiar su estado a
 * "en preparación". El empleado queda registrado como responsable.
 */
public interface TomarPedido {

    Pedido ejecutar(Long idPedido, Long idEmpleado);
}
