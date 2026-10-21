package com.plazoleta.pedido.domain.model;

/**
 * Vista de la cuenta de un cliente que entrega el microservicio de
 * usuarios; el pedido necesita su celular para notificarle el pin por SMS
 * cuando la orden está lista (HU14).
 */
public record CuentaCliente(
        Long id,
        String nombre,
        String celular) {
}
