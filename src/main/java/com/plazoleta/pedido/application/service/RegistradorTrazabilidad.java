package com.plazoleta.pedido.application.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.plazoleta.pedido.domain.api.ConsultarTrazabilidad;
import com.plazoleta.pedido.domain.exception.RecursoNoEncontradoException;
import com.plazoleta.pedido.domain.model.EstadoPedido;
import com.plazoleta.pedido.domain.model.Pedido;
import com.plazoleta.pedido.domain.model.RegistroTrazabilidad;
import com.plazoleta.pedido.domain.spi.RepositorioPedidosPuerto;
import com.plazoleta.pedido.domain.spi.RepositorioTrazabilidadPuerto;

/**
 * Utilidad transversal: cada caso de uso deja aquí el registro del cambio
 * de estado que produjo, como exige la HU17, y el cliente consulta por
 * aquí el historial de sus propios pedidos.
 */
@Service
public class RegistradorTrazabilidad implements ConsultarTrazabilidad {

    private final RepositorioTrazabilidadPuerto repositorioTrazabilidad;
    private final RepositorioPedidosPuerto repositorioPedidos;

    public RegistradorTrazabilidad(RepositorioTrazabilidadPuerto repositorioTrazabilidad,
                                   RepositorioPedidosPuerto repositorioPedidos) {
        this.repositorioTrazabilidad = repositorioTrazabilidad;
        this.repositorioPedidos = repositorioPedidos;
    }

    /**
     * Registra la transición de estado de un pedido ya guardado.
     */
    public void registrar(Pedido pedido, EstadoPedido anterior, EstadoPedido nueva) {
        repositorioTrazabilidad.registrar(new RegistroTrazabilidad(
                null, pedido.id(), anterior, nueva, Instant.now()));
    }

    /**
     * Localiza un pedido o explica con 404 que no existe.
     */
    public Pedido obtenerPedido(Long idPedido) {
        return repositorioPedidos.obtenerPorId(idPedido).orElseThrow(() ->
                new RecursoNoEncontradoException(
                        "No existe un pedido con el identificador " + idPedido));
    }

    @Override
    public List<RegistroTrazabilidad> ejecutar(Long idPedido, Long idCliente) {
        Pedido pedido = obtenerPedido(idPedido);
        if (!pedido.idCliente().equals(idCliente)) {
            throw new RecursoNoEncontradoException(
                    "No existe un pedido tuyo con el identificador " + idPedido);
        }
        return repositorioTrazabilidad.listarPorPedido(idPedido);
    }
}
