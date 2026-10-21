package com.plazoleta.pedido.application.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plazoleta.pedido.domain.exception.ReglaNegocioException;
import com.plazoleta.pedido.domain.exception.RecursoNoEncontradoException;
import com.plazoleta.pedido.domain.model.EstadoPedido;
import com.plazoleta.pedido.domain.model.LineaPedido;
import com.plazoleta.pedido.domain.model.Pedido;
import com.plazoleta.pedido.domain.model.VinculoEmpleado;
import com.plazoleta.pedido.domain.spi.RestaurantesRemotosPuerto;

/**
 * Pruebas de la guarda compartida por los casos de uso del empleado:
 * resuelve el restaurante de la cuenta y exige que el pedido sea de ese
 * restaurante (HU12 a HU15).
 */
@ExtendWith(MockitoExtension.class)
class VerificadorAmbitoEmpleadoTest {

    private static final Long ID_EMPLEADO = 5L;

    @Mock
    private RestaurantesRemotosPuerto restaurantes;

    private VerificadorAmbitoEmpleado verificador;

    @BeforeEach
    void preparar() {
        verificador = new VerificadorAmbitoEmpleado(restaurantes);
    }

    @Test
    void debeResolverElRestauranteDelEmpleado() {
        Mockito.when(restaurantes.consultarVinculoEmpleado(ID_EMPLEADO))
                .thenReturn(new VinculoEmpleado(ID_EMPLEADO, 1L));

        assertEquals(1L, verificador.restauranteDelEmpleado(ID_EMPLEADO));
    }

    @Test
    void debeRechazarCon404CuentasSinVinculoLaboral() {
        Mockito.when(restaurantes.consultarVinculoEmpleado(ID_EMPLEADO)).thenReturn(null);

        assertThatThrownBy(() -> verificador.restauranteDelEmpleado(ID_EMPLEADO))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("vinculada");
    }

    @Test
    void debeAceptarPedidosDelMismoRestaurante() {
        Pedido pedido = Pedido.reconstruir(10L, 4L, 1L, EstadoPedido.PENDIENTE,
                List.of(new LineaPedido(1L, 2)), null, null, Instant.now(), null);

        assertThatCode(() -> verificador.exigirPedidoDelRestaurante(pedido, 1L))
                .doesNotThrowAnyException();
    }

    @Test
    void debeRechazarPedidosDeOtroRestaurante() {
        Pedido ajeno = Pedido.reconstruir(10L, 4L, 2L, EstadoPedido.PENDIENTE,
                List.of(new LineaPedido(1L, 2)), null, null, Instant.now(), null);

        assertThatThrownBy(() -> verificador.exigirPedidoDelRestaurante(ajeno, 1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("otro restaurante");
    }
}
