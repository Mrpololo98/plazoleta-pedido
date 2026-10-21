package com.plazoleta.pedido.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plazoleta.pedido.domain.exception.RecursoNoEncontradoException;
import com.plazoleta.pedido.domain.model.EstadoPedido;
import com.plazoleta.pedido.domain.model.LineaPedido;
import com.plazoleta.pedido.domain.model.Pedido;
import com.plazoleta.pedido.domain.model.RegistroTrazabilidad;
import com.plazoleta.pedido.domain.spi.RepositorioPedidosPuerto;
import com.plazoleta.pedido.domain.spi.RepositorioTrazabilidadPuerto;

/**
 * Pruebas unitarias del caso de uso HU17 (trazabilidad del pedido).
 */
@ExtendWith(MockitoExtension.class)
class RegistradorTrazabilidadTest {

    private static final Long ID_CLIENTE = 4L;

    @Mock
    private RepositorioTrazabilidadPuerto repositorioTrazabilidad;

    @Mock
    private RepositorioPedidosPuerto repositorioPedidos;

    private RegistradorTrazabilidad consultor;

    @BeforeEach
    void preparar() {
        consultor = new RegistradorTrazabilidad(repositorioTrazabilidad, repositorioPedidos);
    }

    @Test
    void debeEntregarElHistorialDelPedidoPropio() {
        Pedido pedido = Pedido.reconstruir(10L, ID_CLIENTE, 1L, EstadoPedido.ENTREGADO,
                List.of(new LineaPedido(1L, 2)), 5L, "hash", Instant.now(), Instant.now());
        when(repositorioPedidos.obtenerPorId(10L)).thenReturn(Optional.of(pedido));
        List<RegistroTrazabilidad> historial = List.of(
                new RegistroTrazabilidad(1L, 10L, null, EstadoPedido.PENDIENTE, Instant.now()),
                new RegistroTrazabilidad(2L, 10L, EstadoPedido.PENDIENTE,
                        EstadoPedido.EN_PREPARACION, Instant.now()));
        when(repositorioTrazabilidad.listarPorPedido(10L)).thenReturn(historial);

        List<RegistroTrazabilidad> resultado = consultor.ejecutar(10L, ID_CLIENTE);

        assertEquals(2, resultado.size());
        assertEquals(EstadoPedido.PENDIENTE, resultado.get(0).estadoNuevo());
    }

    @Test
    void debeRechazarCon404ElHistorialDePedidosAjenos() {
        Pedido deOtro = Pedido.reconstruir(10L, 9L, 1L, EstadoPedido.PENDIENTE,
                List.of(new LineaPedido(1L, 1)), null, null, Instant.now(), null);
        when(repositorioPedidos.obtenerPorId(10L)).thenReturn(Optional.of(deOtro));

        assertThatThrownBy(() -> consultor.ejecutar(10L, ID_CLIENTE))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("tuyo");
    }

    @Test
    void debeRechazarPedidosInexistentes() {
        when(repositorioPedidos.obtenerPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consultor.ejecutar(99L, ID_CLIENTE))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("pedido");
    }
}
