package com.plazoleta.pedido.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
import com.plazoleta.pedido.domain.model.EstadoPedido;
import com.plazoleta.pedido.domain.model.LineaPedido;
import com.plazoleta.pedido.domain.model.Pedido;
import com.plazoleta.pedido.domain.spi.RepositorioPedidosPuerto;

/**
 * Pruebas unitarias del caso de uso HU13 (asignarse un pedido y pasarlo a
 * preparación).
 */
@ExtendWith(MockitoExtension.class)
class TomadorPedidosTest {

    private static final Long ID_EMPLEADO = 5L;
    private static final Long ID_RESTAURANTE = 1L;
    private static final List<LineaPedido> LINEAS = List.of(new LineaPedido(1L, 2));

    @Mock
    private RepositorioPedidosPuerto repositorioPedidos;

    @Mock
    private RegistradorTrazabilidad trazabilidad;

    @Mock
    private VerificadorAmbitoEmpleado verificadorAmbito;

    private TomadorPedidos tomador;

    @BeforeEach
    void preparar() {
        tomador = new TomadorPedidos(repositorioPedidos, trazabilidad, verificadorAmbito);
    }

    private Pedido pedidoPendienteDe(Long idRestaurante) {
        return Pedido.reconstruir(10L, 4L, idRestaurante, EstadoPedido.PENDIENTE,
                LINEAS, null, null, Instant.now(), null);
    }

    @Test
    void debeAsignarAlEmpleadoYPasarElPedidoAPreparacion() {
        Pedido pendiente = pedidoPendienteDe(ID_RESTAURANTE);
        when(trazabilidad.obtenerPedido(10L)).thenReturn(pendiente);
        when(verificadorAmbito.restauranteDelEmpleado(ID_EMPLEADO)).thenReturn(ID_RESTAURANTE);
        when(repositorioPedidos.guardar(any(Pedido.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Pedido tomado = tomador.ejecutar(10L, ID_EMPLEADO);

        assertEquals(EstadoPedido.EN_PREPARACION, tomado.estado());
        assertEquals(ID_EMPLEADO, tomado.idEmpleadoAsignado());
        verify(trazabilidad).registrar(tomado, EstadoPedido.PENDIENTE, EstadoPedido.EN_PREPARACION);
    }

    @Test
    void debeRechazarPedidosDeOtroRestaurante() {
        when(trazabilidad.obtenerPedido(10L)).thenReturn(pedidoPendienteDe(2L));
        when(verificadorAmbito.restauranteDelEmpleado(ID_EMPLEADO)).thenReturn(ID_RESTAURANTE);
        doThrow(new ReglaNegocioException(
                "Ese pedido pertenece a otro restaurante de la plazoleta"))
                .when(verificadorAmbito)
                .exigirPedidoDelRestaurante(any(Pedido.class), eq(ID_RESTAURANTE));

        assertThatThrownBy(() -> tomador.ejecutar(10L, ID_EMPLEADO))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("otro restaurante");
    }

    @Test
    void debeRechazarPedidosQueYaEstanEnPreparacion() {
        Pedido enPreparacion = Pedido.reconstruir(10L, 4L, ID_RESTAURANTE,
                EstadoPedido.EN_PREPARACION, LINEAS, 6L, null, Instant.now(), null);
        when(trazabilidad.obtenerPedido(10L)).thenReturn(enPreparacion);
        when(verificadorAmbito.restauranteDelEmpleado(ID_EMPLEADO)).thenReturn(ID_RESTAURANTE);

        assertThatThrownBy(() -> tomador.ejecutar(10L, ID_EMPLEADO))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("pendientes");
    }

    @Test
    void debeRechazarPedidosInexistentes() {
        when(trazabilidad.obtenerPedido(99L)).thenThrow(
                new com.plazoleta.pedido.domain.exception.RecursoNoEncontradoException(
                        "No existe un pedido con el identificador 99"));

        assertThatThrownBy(() -> tomador.ejecutar(99L, ID_EMPLEADO))
                .isInstanceOf(com.plazoleta.pedido.domain.exception.RecursoNoEncontradoException.class);
    }
}
