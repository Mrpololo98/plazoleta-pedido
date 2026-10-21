package com.plazoleta.pedido.infrastructure.integraciones;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.plazoleta.pedido.infrastructure.integraciones.remoto.CuentaUsuarioRemota;

/**
 * Cliente HTTP del microservicio de usuarios. La ruta corresponde al
 * contrato público documentado en su Swagger:
 *
 *  - GET /usuarios/{id} → vista de la cuenta (celular para el SMS de HU14)
 *
 * El token de la petición entrante viaja en cada llamada gracias al
 * {@link PropagadorAutorizacion}.
 */
@FeignClient(name = "servicio-usuarios-plazoleta", url = "${integraciones.usuario.base-url}")
public interface ClienteUsuariosApi {

    @GetMapping("/usuarios/{id}")
    CuentaUsuarioRemota consultarCuenta(@PathVariable("id") Long id);
}
