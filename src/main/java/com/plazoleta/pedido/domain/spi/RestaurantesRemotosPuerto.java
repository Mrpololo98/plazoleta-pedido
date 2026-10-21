package com.plazoleta.pedido.domain.spi;

import com.plazoleta.pedido.domain.model.PlatoRemoto;
import com.plazoleta.pedido.domain.model.RestauranteRemoto;
import com.plazoleta.pedido.domain.model.VinculoEmpleado;

/**
 * Puerto de salida hacia el microservicio de restaurantes: validación de los
 * platos de una orden (HU11), vínculo laboral del empleado (HU12 a HU15) y
 * dueño del establecimiento (HU18).
 */
public interface RestaurantesRemotosPuerto {

    PlatoRemoto consultarPlato(Long idPlato);

    VinculoEmpleado consultarVinculoEmpleado(Long idUsuario);

    RestauranteRemoto consultarRestaurante(Long idRestaurante);
}
