package com.plazoleta.pedido.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Petición de entrega de un pedido (HU15): el pin de seguridad que se le
 * notificó al cliente por SMS cuando su pedido quedó listo.
 */
public record PeticionEntregarPedido(
        @NotBlank(message = "El pin de seguridad es obligatorio")
        String pinSeguridad) {
}
