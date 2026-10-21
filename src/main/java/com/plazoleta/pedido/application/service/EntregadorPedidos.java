package com.plazoleta.pedido.application.service;

import org.springframework.stereotype.Service;

import com.plazoleta.pedido.domain.api.EntregarPedido;
import com.plazoleta.pedido.domain.exception.ReglaNegocioException;
import com.plazoleta.pedido.domain.model.EstadoPedido;
import com.plazoleta.pedido.domain.model.Pedido;
import com.plazoleta.pedido.domain.spi.CifradorPinPuerto;
import com.plazoleta.pedido.domain.spi.RepositorioPedidosPuerto;

/**
 * Implementación HU15: entregar pedido.
 *
 * Solo un pedido LISTO puede entregarse, y solo con el pin de seguridad que
 * se le notificó al cliente; el pin se compara contra el hash guardado.
 * Con la entrega el ciclo queda cerrado y el pedido toma su fecha de fin.
 */
@Service
public class EntregadorPedidos implements EntregarPedido {

    private final RepositorioPedidosPuerto repositorioPedidos;
    private final RegistradorTrazabilidad trazabilidad;
    private final VerificadorAmbitoEmpleado verificadorAmbito;
    private final CifradorPinPuerto cifrador;

    public EntregadorPedidos(RepositorioPedidosPuerto repositorioPedidos,
                             RegistradorTrazabilidad trazabilidad,
                             VerificadorAmbitoEmpleado verificadorAmbito,
                             CifradorPinPuerto cifrador) {
        this.repositorioPedidos = repositorioPedidos;
        this.trazabilidad = trazabilidad;
        this.verificadorAmbito = verificadorAmbito;
        this.cifrador = cifrador;
    }

    @Override
    public Pedido ejecutar(Long idPedido, Long idEmpleado, String pinSeguridad) {
        Pedido pedido = trazabilidad.obtenerPedido(idPedido);
        Long idRestaurante = verificadorAmbito.restauranteDelEmpleado(idEmpleado);
        verificadorAmbito.exigirPedidoDelRestaurante(pedido, idRestaurante);

        if (pedido.pinSeguridadHash() == null) {
            throw new ReglaNegocioException(
                    "El pedido todavía no está listo: no tiene pin de seguridad");
        }
        if (!cifrador.coincide(pinSeguridad, pedido.pinSeguridadHash())) {
            throw new ReglaNegocioException(
                    "El pin de seguridad no coincide con el enviado al cliente");
        }

        pedido.entregar();
        Pedido guardado = repositorioPedidos.guardar(pedido);
        trazabilidad.registrar(guardado, EstadoPedido.LISTO, EstadoPedido.ENTREGADO);
        return guardado;
    }
}
