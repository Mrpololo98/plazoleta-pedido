package com.plazoleta.pedido.domain.exception;

/**
 * Señala que una regla de negocio del dominio de pedidos no se cumplió.
 * Se traduce en una respuesta HTTP 400.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
