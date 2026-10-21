package com.plazoleta.pedido.application.service;

import org.springframework.stereotype.Service;

import com.plazoleta.pedido.domain.api.CancelarPedido;
import com.plazoleta.pedido.domain.exception.RecursoNoEncontradoException;
import com.plazoleta.pedido.domain.model.EstadoPedido;
import com.plazoleta.pedido.domain.model.Pedido;
import com.plazoleta.pedido.domain.spi.RepositorioPedidosPuerto;

/**
 * Implementación HU16: cancelar pedido.
 *
 * El cliente solo puede cancelar pedidos propios y únicamente si siguen
 * pendientes; si ya entró en preparación el rechazo usa el mensaje textual
 * que pide la historia de usuario.
 */
@Service
public class CanceladorPedidos implements CancelarPedido {

    private final RepositorioPedidosPuerto repositorioPedidos;
    private final RegistradorTrazabilidad trazabilidad;

    public CanceladorPedidos(RepositorioPedidosPuerto repositorioPedidos,
                             RegistradorTrazabilidad trazabilidad) {
        this.repositorioPedidos = repositorioPedidos;
        this.trazabilidad = trazabilidad;
    }

    @Override
    public Pedido ejecutar(Long idPedido, Long idCliente) {
        Pedido pedido = trazabilidad.obtenerPedido(idPedido);
        if (!pedido.idCliente().equals(idCliente)) {
            // No se revela la existencia de pedidos ajenos.
            throw new RecursoNoEncontradoException(
                    "No existe un pedido tuyo con el identificador " + idPedido);
        }

        pedido.cancelar();
        Pedido guardado = repositorioPedidos.guardar(pedido);
        trazabilidad.registrar(guardado, EstadoPedido.PENDIENTE, EstadoPedido.CANCELADO);
        return guardado;
    }
}
