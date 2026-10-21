package com.plazoleta.pedido.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plazoleta.pedido.domain.exception.ReglaNegocioException;
import com.plazoleta.pedido.domain.model.CuentaCliente;
import com.plazoleta.pedido.domain.model.EstadoPedido;
import com.plazoleta.pedido.domain.model.LineaPedido;
import com.plazoleta.pedido.domain.model.Pedido;
import com.plazoleta.pedido.domain.model.PedidoListo;
import com.plazoleta.pedido.domain.spi.CifradorPinPuerto;
import com.plazoleta.pedido.domain.spi.NotificadorSmsPuerto;
import com.plazoleta.pedido.domain.spi.RepositorioPedidosPuerto;
import com.plazoleta.pedido.domain.spi.UsuariosRemotosPuerto;

/**
 * Pruebas unitarias del caso de uso HU14 (notificar que el pedido está
 * listo, con pin de seguridad y SMS al cliente).
 */
@ExtendWith(MockitoExtension.class)
class MarcadorPedidosListosTest {

    private static final Long ID_EMPLEADO = 5L;
    private static final Long ID_RESTAURANTE = 1L;
    private static final List<LineaPedido> LINEAS = List.of(new LineaPedido(1L, 2));

    @Mock
    private RepositorioPedidosPuerto repositorioPedidos;

    @Mock
    private RegistradorTrazabilidad trazabilidad;

    @Mock
    private VerificadorAmbitoEmpleado verificadorAmbito;

    @Mock
    private UsuariosRemotosPuerto usuarios;

    @Mock
    private NotificadorSmsPuerto notificadorSms;

    @Mock
    private CifradorPinPuerto cifrador;

    private MarcadorPedidosListos marcador;

    @BeforeEach
    void preparar() {
        marcador = new MarcadorPedidosListos(repositorioPedidos, trazabilidad,
                verificadorAmbito, usuarios, notificadorSms, cifrador,
                new GeneradorPinSeguridad());
    }

    private Pedido pedidoEnPreparacion() {
        return Pedido.reconstruir(10L, 4L, ID_RESTAURANTE, EstadoPedido.EN_PREPARACION,
                LINEAS, ID_EMPLEADO, null, Instant.now(), null);
    }

    @Test
    void debeMarcarListoNotificarElSmsYGuardarElPinCifrado() {
        Pedido enPreparacion = pedidoEnPreparacion();
        when(trazabilidad.obtenerPedido(10L)).thenReturn(enPreparacion);
        when(verificadorAmbito.restauranteDelEmpleado(ID_EMPLEADO)).thenReturn(ID_RESTAURANTE);
        when(cifrador.cifrar(org.mockito.ArgumentMatchers.anyString())).thenReturn("hash-del-pin");
        when(usuarios.consultarCuenta(4L)).thenReturn(new CuentaCliente(4L, "Valentina Cruz", "+573004440001"));
        when(repositorioPedidos.guardar(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        PedidoListo listo = marcador.ejecutar(10L, ID_EMPLEADO);

        assertEquals(EstadoPedido.LISTO, listo.pedido().estado());
        assertEquals("+573004440001", listo.telefonoNotificado());
        assertEquals(6, listo.pinSeguridad().length());
        verify(notificadorSms).notificarPedidoListo("+573004440001", 10L, listo.pinSeguridad());
        verify(trazabilidad).registrar(listo.pedido(), EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO);
    }

    @Test
    void debeRechazarMarcarListoUnPedidoPendiente() {
        Pedido pendiente = Pedido.reconstruir(10L, 4L, ID_RESTAURANTE, EstadoPedido.PENDIENTE,
                LINEAS, null, null, Instant.now(), null);
        when(trazabilidad.obtenerPedido(10L)).thenReturn(pendiente);
        when(verificadorAmbito.restauranteDelEmpleado(ID_EMPLEADO)).thenReturn(ID_RESTAURANTE);

        assertThatThrownBy(() -> marcador.ejecutar(10L, ID_EMPLEADO))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("preparación");
    }

    @Test
    void debeRechazarPedidosDeOtroRestaurante() {
        Pedido ajeno = Pedido.reconstruir(10L, 4L, 2L, EstadoPedido.EN_PREPARACION,
                LINEAS, 6L, null, Instant.now(), null);
        when(trazabilidad.obtenerPedido(10L)).thenReturn(ajeno);
        when(verificadorAmbito.restauranteDelEmpleado(ID_EMPLEADO)).thenReturn(ID_RESTAURANTE);
        doThrow(new ReglaNegocioException(
                "Ese pedido pertenece a otro restaurante de la plazoleta"))
                .when(verificadorAmbito)
                .exigirPedidoDelRestaurante(any(Pedido.class), eq(ID_RESTAURANTE));

        assertThatThrownBy(() -> marcador.ejecutar(10L, ID_EMPLEADO))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("otro restaurante");
    }
}
