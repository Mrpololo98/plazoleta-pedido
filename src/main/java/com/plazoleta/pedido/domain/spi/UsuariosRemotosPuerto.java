package com.plazoleta.pedido.domain.spi;

import com.plazoleta.pedido.domain.model.CuentaCliente;

/**
 * Puerto de salida hacia el microservicio de usuarios: la cuenta del cliente
 * se consulta para notificarle por SMS el pin de seguridad (HU14).
 */
public interface UsuariosRemotosPuerto {

    CuentaCliente consultarCuenta(Long idCliente);
}
