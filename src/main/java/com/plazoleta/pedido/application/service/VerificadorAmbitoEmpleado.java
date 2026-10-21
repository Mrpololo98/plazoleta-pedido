package com.plazoleta.pedido.application.service;

import org.springframework.stereotype.Component;

import com.plazoleta.pedido.domain.exception.ReglaNegocioException;
import com.plazoleta.pedido.domain.exception.RecursoNoEncontradoException;
import com.plazoleta.pedido.domain.model.Pedido;
import com.plazoleta.pedido.domain.model.VinculoEmpleado;
import com.plazoleta.pedido.domain.spi.RestaurantesRemotosPuerto;

/**
 * Guarda compartida por los casos de uso del empleado (HU12 a HU15):
 * resuelve a qué restaurante pertenece la cuenta y comprueba que un pedido
 * sea de ese restaurante antes de dejarlo tocar.
 */
@Component
public class VerificadorAmbitoEmpleado {

    private final RestaurantesRemotosPuerto restaurantes;

    public VerificadorAmbitoEmpleado(RestaurantesRemotosPuerto restaurantes) {
        this.restaurantes = restaurantes;
    }

    /**
     * Identificador del restaurante al que pertenece la cuenta del empleado.
     */
    public Long restauranteDelEmpleado(Long idUsuario) {
        VinculoEmpleado vinculo = restaurantes.consultarVinculoEmpleado(idUsuario);
        if (vinculo == null) {
            throw new RecursoNoEncontradoException(
                    "La cuenta " + idUsuario + " no está vinculada a ningún restaurante");
        }
        return vinculo.idRestaurante();
    }

    /**
     * Comprueba que el pedido pertenezca al restaurante del empleado.
     */
    public void exigirPedidoDelRestaurante(Pedido pedido, Long idRestaurante) {
        if (!pedido.idRestaurante().equals(idRestaurante)) {
            throw new ReglaNegocioException(
                    "Ese pedido pertenece a otro restaurante de la plazoleta");
        }
    }
}
