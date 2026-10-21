package com.plazoleta.pedido.domain.exception;

/**
 * Señala que el pedido o el recurso remoto consultado no existe.
 * Se traduce en una respuesta HTTP 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
