package com.plazoleta.pedido.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plazoleta.pedido.domain.exception.ReglaNegocioException;
import com.plazoleta.pedido.domain.exception.RecursoNoEncontradoException;
import com.plazoleta.pedido.domain.model.EstadoPedido;
import com.plazoleta.pedido.domain.model.LineaPedido;
import com.plazoleta.pedido.domain.model.Pedido;
import com.plazoleta.pedido.domain.model.PlatoRemoto;
import com.plazoleta.pedido.domain.spi.RepositorioPedidosPuerto;
import com.plazoleta.pedido.domain.spi.RestaurantesRemotosPuerto;

/**
 * Pruebas unitarias del caso de uso HU11 (realizar pedido).
 */
@ExtendWith(MockitoExtension.class)
class CreadorPedidosTest {

    private static final Long ID_CLIENTE = 4L;
    private static final Long ID_RESTAURANTE = 1L;

    @Mock
    private RepositorioPedidosPuerto repositorioPedidos;

    @Mock
    private RestaurantesRemotosPuerto restaurantes;

    @Mock
    private RegistradorTrazabilidad trazabilidad;

    private CreadorPedidos creador;

    @BeforeEach
    void preparar() {
        creador = new CreadorPedidos(repositorioPedidos, restaurantes, trazabilidad);
    }

    private void cartaValida() {
        when(restaurantes.consultarPlato(1L)).thenReturn(new PlatoRemoto(1L, ID_RESTAURANTE, true));
        when(restaurantes.consultarPlato(3L)).thenReturn(new PlatoRemoto(3L, ID_RESTAURANTE, true));
    }

    @Test
    void debeCrearUnPedidoPendienteConSuPrimerRegistroDeTrazabilidad() {
        cartaValida();
        when(repositorioPedidos.existePedidoActivoDe(ID_CLIENTE)).thenReturn(false);
        when(repositorioPedidos.guardar(org.mockito.ArgumentMatchers.any(Pedido.class)))
                .thenAnswer(invocacion -> {
                    Pedido pedido = invocacion.getArgument(0);
                    return Pedido.reconstruir(10L, pedido.idCliente(), pedido.idRestaurante(),
                            pedido.estado(), pedido.lineas(), pedido.idEmpleadoAsignado(),
                            pedido.pinSeguridadHash(), pedido.fechaCreacion(), pedido.fechaCierre());
                });

        Pedido guardado = creador.ejecutar(ID_CLIENTE, ID_RESTAURANTE,
                List.of(new LineaPedido(1L, 2), new LineaPedido(3L, 1)));

        assertEquals(10L, guardado.id());
        assertEquals(EstadoPedido.PENDIENTE, guardado.estado());
        assertEquals(2, guardado.lineas().size());
        verify(trazabilidad).registrar(guardado, null, EstadoPedido.PENDIENTE);
    }

    @Test
    void debeRechazarPedidosSinPlatos() {
        assertThatThrownBy(() -> creador.ejecutar(ID_CLIENTE, ID_RESTAURANTE, List.of()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("al menos un plato");
        verifyNoInteractions(repositorioPedidos);
    }

    @Test
    void debeRechazarCantidadesMenoresQueUno() {
        assertThatThrownBy(() -> creador.ejecutar(ID_CLIENTE, ID_RESTAURANTE,
                List.of(new LineaPedido(1L, 0))))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("cantidad");
        verifyNoInteractions(repositorioPedidos);
    }

    @Test
    void debeRechazarCuandoElClienteYaTieneUnPedidoEnCurso() {
        when(repositorioPedidos.existePedidoActivoDe(ID_CLIENTE)).thenReturn(true);

        assertThatThrownBy(() -> creador.ejecutar(ID_CLIENTE, ID_RESTAURANTE,
                List.of(new LineaPedido(1L, 1))))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("pedido en curso");
    }

    @Test
    void debeRechazarPlatosDeOtrosRestaurantes() {
        when(restaurantes.consultarPlato(1L)).thenReturn(new PlatoRemoto(1L, 1L, true));
        when(restaurantes.consultarPlato(5L)).thenReturn(new PlatoRemoto(5L, 2L, true));

        assertThatThrownBy(() -> creador.ejecutar(ID_CLIENTE, ID_RESTAURANTE,
                List.of(new LineaPedido(1L, 1), new LineaPedido(5L, 1))))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("mismo establecimiento");
    }

    @Test
    void debeRechazarPlatosInhabilitadosDeLaCarta() {
        when(restaurantes.consultarPlato(1L)).thenReturn(new PlatoRemoto(1L, 1L, false));

        assertThatThrownBy(() -> creador.ejecutar(ID_CLIENTE, ID_RESTAURANTE,
                List.of(new LineaPedido(1L, 1))))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no está disponible");
    }

    @Test
    void debeRechazarPlatosInexistentes() {
        when(restaurantes.consultarPlato(99L)).thenThrow(new RecursoNoEncontradoException(
                "No existe un plato con el identificador 99"));

        assertThatThrownBy(() -> creador.ejecutar(ID_CLIENTE, ID_RESTAURANTE,
                List.of(new LineaPedido(99L, 1))))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("plato");
    }

    @Test
    void debeRechazarLineasNulas() {
        assertThatThrownBy(() -> creador.ejecutar(ID_CLIENTE, ID_RESTAURANTE, null))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("al menos un plato");
        verifyNoInteractions(repositorioPedidos);
    }
}
