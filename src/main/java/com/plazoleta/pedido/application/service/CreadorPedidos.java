package com.plazoleta.pedido.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.plazoleta.pedido.domain.api.CrearPedido;
import com.plazoleta.pedido.domain.exception.ReglaNegocioException;
import com.plazoleta.pedido.domain.model.EstadoPedido;
import com.plazoleta.pedido.domain.model.LineaPedido;
import com.plazoleta.pedido.domain.model.Pedido;
import com.plazoleta.pedido.domain.model.PlatoRemoto;
import com.plazoleta.pedido.domain.spi.RepositorioPedidosPuerto;
import com.plazoleta.pedido.domain.spi.RestaurantesRemotosPuerto;

/**
 * Implementación HU11: realizar pedido.
 *
 * Reglas: la orden lleva platos de un solo restaurante (todos habilitados
 * en la carta), las cantidades son positivas y el cliente no puede abrir
 * una nueva mientras tenga otra en proceso (pendiente, en preparación o
 * lista). El pedido nace con estado PENDIENTE y su primer registro de
 * trazabilidad.
 */
@Service
public class CreadorPedidos implements CrearPedido {

    private final RepositorioPedidosPuerto repositorioPedidos;
    private final RestaurantesRemotosPuerto restaurantes;
    private final RegistradorTrazabilidad trazabilidad;

    public CreadorPedidos(RepositorioPedidosPuerto repositorioPedidos,
                          RestaurantesRemotosPuerto restaurantes,
                          RegistradorTrazabilidad trazabilidad) {
        this.repositorioPedidos = repositorioPedidos;
        this.restaurantes = restaurantes;
        this.trazabilidad = trazabilidad;
    }

    @Override
    public Pedido ejecutar(Long idCliente, Long idRestaurante, List<LineaPedido> lineas) {
        if (lineas == null || lineas.isEmpty()) {
            throw new ReglaNegocioException("El pedido debe llevar al menos un plato");
        }
        if (lineas.stream().anyMatch(linea -> linea.cantidad() <= 0)) {
            throw new ReglaNegocioException("La cantidad de cada plato debe ser mayor que cero");
        }
        if (repositorioPedidos.existePedidoActivoDe(idCliente)) {
            throw new ReglaNegocioException(
                    "Ya tienes un pedido en curso: espera a que termine o cancela el pendiente");
        }
        validarCarta(idRestaurante, lineas);

        Pedido guardado = repositorioPedidos.guardar(
                Pedido.crear(idCliente, idRestaurante, lineas));
        trazabilidad.registrar(guardado, null, EstadoPedido.PENDIENTE);
        return guardado;
    }

    private void validarCarta(Long idRestaurante, List<LineaPedido> lineas) {
        for (LineaPedido linea : lineas) {
            PlatoRemoto plato = restaurantes.consultarPlato(linea.idPlato());
            if (!plato.idRestaurante().equals(idRestaurante)) {
                throw new ReglaNegocioException(
                        "El plato " + linea.idPlato() + " no pertenece al restaurante del pedido: "
                                + "una orden solo puede llevar platos de un mismo establecimiento");
            }
            if (!plato.activo()) {
                throw new ReglaNegocioException(
                        "El plato " + linea.idPlato() + " no está disponible en la carta");
            }
        }
    }
}
