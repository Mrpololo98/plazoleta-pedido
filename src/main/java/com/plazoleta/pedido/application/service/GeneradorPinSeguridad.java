package com.plazoleta.pedido.application.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

/**
 * Genera el pin de seguridad de seis dígitos que protege la entrega de un
 * pedido (HU14 y HU15).
 */
@Component
public class GeneradorPinSeguridad {

    private static final SecureRandom AZAR = new SecureRandom();

    public String generar() {
        return String.format("%06d", AZAR.nextInt(1_000_000));
    }
}
