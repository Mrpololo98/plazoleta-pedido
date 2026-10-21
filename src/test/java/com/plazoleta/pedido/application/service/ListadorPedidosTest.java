package com.plazoleta.pedido.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plazoleta.pedido.domain.exception.ReglaNegocioException;
import com.plazoleta.pedido.domain.model.EstadoPedido;
import com.plazoleta.pedido.domain.model.HojaPedidos;
import com.plazoleta.pedido.domain.spi.RepositorioPedidosPuerto;

/**
 * Pruebas unitarias del caso de uso HU12 (lista de pedidos por estado del
 * restaurante del empleado).
 */
@ExtendWith(MockitoExtension.class)
class ListadorPedidosTest {

    private static final Long ID_EMPLEADO = 5L;
    private static final Long ID_RESTAURANTE = 1L;

    @Mock
    private RepositorioPedidosPuerto repositorioPedidos;

    @Mock
    private VerificadorAmbitoEmpleado verificadorAmbito;

    private ListadorPedidos listador;

    @BeforeEach
    void preparar() {
        listador = new ListadorPedidos(repositorioPedidos, verificadorAmbito);
    }

    @Test
    void debeListarLosPedidosDelRestauranteDelEmpleado() {
        HojaPedidos hoja = new HojaPedidos(java.util.List.of(), 0, 10, 0L);
        when(verificadorAmbito.restauranteDelEmpleado(ID_EMPLEADO)).thenReturn(ID_RESTAURANTE);
        when(repositorioPedidos.buscarPorRestauranteYEstado(
                ID_RESTAURANTE, EstadoPedido.PENDIENTE, 0, 10)).thenReturn(hoja);

        HojaPedidos resultado = listador.ejecutar(ID_EMPLEADO, EstadoPedido.PENDIENTE, 0, 10);

        assertSame(hoja, resultado);
    }

    @Test
    void debeRechazarPaginacionesInvalidas() {
        assertThatThrownBy(() -> listador.ejecutar(ID_EMPLEADO, EstadoPedido.LISTO, -1, 10))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("negativo");

        assertThatThrownBy(() -> listador.ejecutar(ID_EMPLEADO, EstadoPedido.LISTO, 0, 0))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("cantidad");

        verifyNoInteractions(repositorioPedidos);
    }
}
