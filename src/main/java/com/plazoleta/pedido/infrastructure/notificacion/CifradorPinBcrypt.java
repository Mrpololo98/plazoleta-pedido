package com.plazoleta.pedido.infrastructure.notificacion;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.plazoleta.pedido.domain.spi.CifradorPinPuerto;

/**
 * Protege el pin de seguridad de los pedidos con BCrypt (HU14 y HU15): el
 * pin viaja al cliente por SMS y aquí solo queda su hash; la entrega
 * compara el pin digitado contra ese hash.
 */
@Component
public class CifradorPinBcrypt implements CifradorPinPuerto {

    private final BCryptPasswordEncoder codificador = new BCryptPasswordEncoder();

    @Override
    public String cifrar(String pin) {
        return codificador.encode(pin);
    }

    @Override
    public boolean coincide(String pin, String hash) {
        return codificador.matches(pin, hash);
    }
}
