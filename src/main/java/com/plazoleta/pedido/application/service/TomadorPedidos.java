package com.plazoleta.pedido.application.service;

import org.springframework.stereotype.Service;

import com.plazoleta.pedido.domain.api.TomarPedido;
import com.plazoleta.pedido.domain.model.EstadoPedido;
import com.plazoleta.pedido.domain.model.Pedido;
import com.plazoleta.pedido.domain.spi.RepositorioPedidosPuerto;

/**
 * Implementación HU13: el empleado se asigna un pedido y este pasa a
 * "en preparación".
 *
 * Solo aplica a pedidos pendientes del restaurante del empleado; el
 * responsable queda registrado en el campo de empleado asignado.
 */
@Service
public class TomadorPedidos implements TomarPedido {

    private final RepositorioPedidosPuerto repositorioPedidos;
    private final RegistradorTrazabilidad trazabilidad;
    private final VerificadorAmbitoEmpleado verificadorAmbito;

    public TomadorPedidos(RepositorioPedidosPuerto repositorioPedidos,
                          RegistradorTrazabilidad trazabilidad,
                          VerificadorAmbitoEmpleado verificadorAmbito) {
        this.repositorioPedidos = repositorioPedidos;
        this.trazabilidad = trazabilidad;
        this.verificadorAmbito = verificadorAmbito;
    }

    @Override
    public Pedido ejecutar(Long idPedido, Long idEmpleado) {
        Pedido pedido = trazabilidad.obtenerPedido(idPedido);
        Long idRestaurante = verificadorAmbito.restauranteDelEmpleado(idEmpleado);
        verificadorAmbito.exigirPedidoDelRestaurante(pedido, idRestaurante);

        pedido.asignarEmpleado(idEmpleado);
        Pedido guardado = repositorioPedidos.guardar(pedido);
        trazabilidad.registrar(guardado, EstadoPedido.PENDIENTE, EstadoPedido.EN_PREPARACION);
        return guardado;
    }
}
