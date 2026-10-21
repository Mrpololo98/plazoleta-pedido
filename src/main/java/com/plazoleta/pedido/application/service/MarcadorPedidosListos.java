package com.plazoleta.pedido.application.service;

import org.springframework.stereotype.Service;

import com.plazoleta.pedido.domain.api.MarcarPedidoListo;
import com.plazoleta.pedido.domain.exception.ReglaNegocioException;
import com.plazoleta.pedido.domain.model.CuentaCliente;
import com.plazoleta.pedido.domain.model.EstadoPedido;
import com.plazoleta.pedido.domain.model.Pedido;
import com.plazoleta.pedido.domain.model.PedidoListo;
import com.plazoleta.pedido.domain.spi.CifradorPinPuerto;
import com.plazoleta.pedido.domain.spi.NotificadorSmsPuerto;
import com.plazoleta.pedido.domain.spi.RepositorioPedidosPuerto;
import com.plazoleta.pedido.domain.spi.UsuariosRemotosPuerto;

/**
 * Implementación HU14: notificar que el pedido está listo.
 *
 * El pedido pasa a LISTO, se genera un pin de seguridad de seis dígitos que
 * queda guardado cifrado y se notifica al cliente por SMS al teléfono de su
 * cuenta; el pin es la llave para reclamar la entrega (HU15).
 */
@Service
public class MarcadorPedidosListos implements MarcarPedidoListo {

    private final RepositorioPedidosPuerto repositorioPedidos;
    private final RegistradorTrazabilidad trazabilidad;
    private final VerificadorAmbitoEmpleado verificadorAmbito;
    private final UsuariosRemotosPuerto usuarios;
    private final NotificadorSmsPuerto notificadorSms;
    private final CifradorPinPuerto cifrador;
    private final GeneradorPinSeguridad generadorPin;

    public MarcadorPedidosListos(RepositorioPedidosPuerto repositorioPedidos,
                                 RegistradorTrazabilidad trazabilidad,
                                 VerificadorAmbitoEmpleado verificadorAmbito,
                                 UsuariosRemotosPuerto usuarios,
                                 NotificadorSmsPuerto notificadorSms,
                                 CifradorPinPuerto cifrador,
                                 GeneradorPinSeguridad generadorPin) {
        this.repositorioPedidos = repositorioPedidos;
        this.trazabilidad = trazabilidad;
        this.verificadorAmbito = verificadorAmbito;
        this.usuarios = usuarios;
        this.notificadorSms = notificadorSms;
        this.cifrador = cifrador;
        this.generadorPin = generadorPin;
    }

    @Override
    public PedidoListo ejecutar(Long idPedido, Long idEmpleado) {
        Pedido pedido = trazabilidad.obtenerPedido(idPedido);
        Long idRestaurante = verificadorAmbito.restauranteDelEmpleado(idEmpleado);
        verificadorAmbito.exigirPedidoDelRestaurante(pedido, idRestaurante);

        pedido.marcarListo();
        String pin = generadorPin.generar();
        pedido.fijarPinSeguridad(cifrador.cifrar(pin));

        CuentaCliente cliente = usuarios.consultarCuenta(pedido.idCliente());
        notificadorSms.notificarPedidoListo(cliente.celular(), pedido.id(), pin);

        Pedido guardado = repositorioPedidos.guardar(pedido);
        trazabilidad.registrar(guardado, EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO);
        return new PedidoListo(guardado, pin, cliente.celular());
    }
}
