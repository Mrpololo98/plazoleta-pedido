package com.plazoleta.pedido.domain.spi;

/**
 * Puerto de salida para proteger el pin de seguridad de un pedido (HU14 y
 * HU15): el pin se guarda cifrado y la entrega lo compara contra el hash.
 */
public interface CifradorPinPuerto {

    String cifrar(String pin);

    boolean coincide(String pin, String hash);
}
