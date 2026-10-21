package com.plazoleta.pedido.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plazoleta.pedido.domain.exception.RecursoNoEncontradoException;
import com.plazoleta.pedido.domain.exception.ReglaNegocioException;
import com.plazoleta.pedido.domain.model.EstadoPedido;
import com.plazoleta.pedido.domain.model.LineaPedido;
import com.plazoleta.pedido.domain.model.Pedido;
import com.plazoleta.pedido.domain.spi.RepositorioPedidosPuerto;

/**
 * Pruebas unitarias del caso de uso HU16 (cancelar pedido).
 */
@ExtendWith(MockitoExtension.class)
class CanceladorPedidosTest {

    private static final Long ID_CLIENTE = 4L;
    private static final List<LineaPedido> LINEAS = List.of(new LineaPedido(1L, 2));

    @Mock
    private RepositorioPedidosPuerto repositorioPedidos;

    @Mock
    private RegistradorTrazabilidad trazabilidad;

    private CanceladorPedidos cancelador;

    @BeforeEach
    void preparar() {
        cancelador = new CanceladorPedidos(repositorioPedidos, trazabilidad);
    }

    @Test
    void debeCancelarUnPedidoPendientePropio() {
        Pedido pendiente = Pedido.reconstruir(10L, ID_CLIENTE, 1L, EstadoPedido.PENDIENTE,
                LINEAS, null, null, Instant.now(), null);
        when(trazabilidad.obtenerPedido(10L)).thenReturn(pendiente);
        when(repositorioPedidos.guardar(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        Pedido cancelado = cancelador.ejecutar(10L, ID_CLIENTE);

        assertEquals(EstadoPedido.CANCELADO, cancelado.estado());
        verify(trazabilidad).registrar(cancelado, EstadoPedido.PENDIENTE, EstadoPedido.CANCELADO);
    }

    @Test
    void debeRechazarCon404LaCancelacionDePedidosAjenos() {
        Pedido deOtro = Pedido.reconstruir(10L, 9L, 1L, EstadoPedido.PENDIENTE,
                LINEAS, null, null, Instant.now(), null);
        when(trazabilidad.obtenerPedido(10L)).thenReturn(deOtro);

        assertThatThrownBy(() -> cancelador.ejecutar(10L, ID_CLIENTE))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("tuyo");
    }

    @Test
    void debeRechazarConElMensajeTextualCuandoYaEstaEnPreparacion() {
        Pedido enPreparacion = Pedido.reconstruir(10L, ID_CLIENTE, 1L,
                EstadoPedido.EN_PREPARACION, LINEAS, 5L, null, Instant.now(), null);
        when(trazabilidad.obtenerPedido(10L)).thenReturn(enPreparacion);

        assertThatThrownBy(() -> cancelador.ejecutar(10L, ID_CLIENTE))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage(Pedido.MENSAJE_CANCELACION_INVALIDA);
    }
}
