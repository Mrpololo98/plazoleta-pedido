package com.plazoleta.pedido.domain.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.plazoleta.pedido.domain.exception.ReglaNegocioException;

/**
 * Pruebas de la máquina de estados del pedido: transiciones permitidas y
 * rechazos con el mensaje que piden las historias de usuario.
 */
class PedidoTest {

    private static final List<LineaPedido> LINEAS = List.of(new LineaPedido(1L, 2));

    private Pedido pedidoEnEstado(EstadoPedido estado) {
        return Pedido.reconstruir(1L, 4L, 1L, estado, LINEAS, 5L, "hash",
                Instant.now().minusSeconds(600), null);
    }

    @Test
    void debeNacerPendienteConFechaDeInicio() {
        Pedido nuevo = Pedido.crear(4L, 1L, LINEAS);

        assertEquals(EstadoPedido.PENDIENTE, nuevo.estado());
        assertNotNull(nuevo.fechaCreacion());
        assertNull(nuevo.fechaCierre());
    }

    @Test
    void debeRechazarLaCancelacionConElMensajeTextualDeLaHU16() {
        Pedido enPreparacion = pedidoEnEstado(EstadoPedido.EN_PREPARACION);

        assertThatThrownBy(enPreparacion::cancelar)
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage(Pedido.MENSAJE_CANCELACION_INVALIDA);
    }

    @Test
    void debeRechazarAsignarUnPedidoQueYaEstaEnPreparacion() {
        Pedido enPreparacion = pedidoEnEstado(EstadoPedido.EN_PREPARACION);

        assertThatThrownBy(() -> enPreparacion.asignarEmpleado(6L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("pendientes");
    }

    @Test
    void debeRechazarMarcarListoUnPedidoPendiente() {
        Pedido pendiente = pedidoEnEstado(EstadoPedido.PENDIENTE);

        assertThatThrownBy(pendiente::marcarListo)
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("preparación");
    }

    @Test
    void debeRechazarEntregarUnPedidoQueNoEstaListo() {
        Pedido enPreparacion = pedidoEnEstado(EstadoPedido.EN_PREPARACION);

        assertThatThrownBy(enPreparacion::entregar)
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("listos");
    }

    @Test
    void debeCerrarElCicloAlEntregarYCalcularSuDuracion() {
        Pedido listo = pedidoEnEstado(EstadoPedido.LISTO);

        listo.entregar();

        assertEquals(EstadoPedido.ENTREGADO, listo.estado());
        assertNotNull(listo.fechaCierre());
        assertEquals(10, listo.duracionMinutos());
    }
}
