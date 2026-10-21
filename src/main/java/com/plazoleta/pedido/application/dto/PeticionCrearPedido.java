package com.plazoleta.pedido.application.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Petición de un pedido nuevo (HU11): el restaurante y los platos con su
 * cantidad. Todos los platos deben ser de ese mismo restaurante.
 */
public record PeticionCrearPedido(
        @NotNull(message = "El identificador del restaurante es obligatorio")
        Long idRestaurante,

        @NotEmpty(message = "El pedido debe llevar al menos un plato")
        @Valid
        List<LineaPeticion> platos) {

    /**
     * Un plato escogido y cuántas unidades del mismo se piden.
     */
    public record LineaPeticion(
            @NotNull(message = "El identificador del plato es obligatorio")
            Long idPlato,

            @Min(value = 1, message = "La cantidad de cada plato debe ser al menos uno")
            int cantidad) {
    }
}
