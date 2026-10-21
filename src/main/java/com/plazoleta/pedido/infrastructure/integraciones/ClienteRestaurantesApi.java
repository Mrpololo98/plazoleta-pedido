package com.plazoleta.pedido.infrastructure.integraciones;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.plazoleta.pedido.infrastructure.integraciones.remoto.PlatoCartaRemoto;
import com.plazoleta.pedido.infrastructure.integraciones.remoto.RestauranteRemotoRespuesta;
import com.plazoleta.pedido.infrastructure.integraciones.remoto.VinculoEmpleadoRemoto;

/**
 * Cliente HTTP del microservicio de restaurantes. Las rutas corresponden
 * al contrato público documentado en su Swagger:
 *
 *  - GET /platos/{id}          → ficha del plato (validación de la orden, HU11)
 *  - GET /empleados/{idUsuario} → vínculo laboral (HU12 a HU15)
 *  - GET /restaurantes/{id}    → ficha con el dueño (HU18)
 *
 * El token de la petición entrante viaja en cada llamada gracias al
 * {@link PropagadorAutorizacion}.
 */
@FeignClient(name = "servicio-restaurantes-plazoleta", url = "${integraciones.restaurante.base-url}")
public interface ClienteRestaurantesApi {

    @GetMapping("/platos/{id}")
    PlatoCartaRemoto consultarPlato(@PathVariable("id") Long id);

    @GetMapping("/empleados/{idUsuario}")
    VinculoEmpleadoRemoto consultarVinculoEmpleado(@PathVariable("idUsuario") Long idUsuario);

    @GetMapping("/restaurantes/{id}")
    RestauranteRemotoRespuesta consultarRestaurante(@PathVariable("id") Long id);
}
