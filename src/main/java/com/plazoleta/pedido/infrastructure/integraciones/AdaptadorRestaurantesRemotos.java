package com.plazoleta.pedido.infrastructure.integraciones;

import org.springframework.stereotype.Component;

import com.plazoleta.pedido.domain.exception.RecursoNoEncontradoException;
import com.plazoleta.pedido.domain.exception.ReglaNegocioException;
import com.plazoleta.pedido.domain.model.PlatoRemoto;
import com.plazoleta.pedido.domain.model.RestauranteRemoto;
import com.plazoleta.pedido.domain.model.VinculoEmpleado;
import com.plazoleta.pedido.domain.spi.RestaurantesRemotosPuerto;
import com.plazoleta.pedido.infrastructure.integraciones.remoto.PlatoCartaRemoto;
import com.plazoleta.pedido.infrastructure.integraciones.remoto.RestauranteRemotoRespuesta;
import com.plazoleta.pedido.infrastructure.integraciones.remoto.VinculoEmpleadoRemoto;

import feign.FeignException;

/**
 * Adaptador de salida: cumple el puerto de restaurantes remotos usando el
 * cliente Feign. Traduce los fallos HTTP del otro microservicio en las
 * excepciones de negocio de este dominio.
 */
@Component
public class AdaptadorRestaurantesRemotos implements RestaurantesRemotosPuerto {

    private final ClienteRestaurantesApi clienteRestaurantes;

    public AdaptadorRestaurantesRemotos(ClienteRestaurantesApi clienteRestaurantes) {
        this.clienteRestaurantes = clienteRestaurantes;
    }

    @Override
    public PlatoRemoto consultarPlato(Long idPlato) {
        try {
            PlatoCartaRemoto plato = clienteRestaurantes.consultarPlato(idPlato);
            return new PlatoRemoto(plato.id(), plato.idRestaurante(),
                    Boolean.TRUE.equals(plato.activo()));
        } catch (FeignException.NotFound platoAusente) {
            throw new RecursoNoEncontradoException(
                    "No existe un plato con el identificador " + idPlato);
        } catch (FeignException servicioCaido) {
            throw new ReglaNegocioException(
                    "El servicio de restaurantes no está disponible, intente de nuevo más tarde");
        }
    }

    @Override
    public VinculoEmpleado consultarVinculoEmpleado(Long idUsuario) {
        try {
            VinculoEmpleadoRemoto vinculo = clienteRestaurantes.consultarVinculoEmpleado(idUsuario);
            return new VinculoEmpleado(vinculo.idUsuario(), vinculo.idRestaurante());
        } catch (FeignException.NotFound vinculoAusente) {
            throw new RecursoNoEncontradoException(
                    "La cuenta " + idUsuario + " no está vinculada como empleado de ningún restaurante");
        } catch (FeignException servicioCaido) {
            throw new ReglaNegocioException(
                    "El servicio de restaurantes no está disponible, intente de nuevo más tarde");
        }
    }

    @Override
    public RestauranteRemoto consultarRestaurante(Long idRestaurante) {
        try {
            RestauranteRemotoRespuesta restaurante =
                    clienteRestaurantes.consultarRestaurante(idRestaurante);
            return new RestauranteRemoto(restaurante.id(), restaurante.idPropietario());
        } catch (FeignException.NotFound restauranteAusente) {
            throw new RecursoNoEncontradoException(
                    "No existe un restaurante con el identificador " + idRestaurante);
        } catch (FeignException servicioCaido) {
            throw new ReglaNegocioException(
                    "El servicio de restaurantes no está disponible, intente de nuevo más tarde");
        }
    }
}
