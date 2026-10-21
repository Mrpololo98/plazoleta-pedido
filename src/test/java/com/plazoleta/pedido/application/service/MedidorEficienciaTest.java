package com.plazoleta.pedido.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.plazoleta.pedido.domain.model.ReporteEficiencia;
import com.plazoleta.pedido.domain.model.RestauranteRemoto;
import com.plazoleta.pedido.domain.spi.RepositorioPedidosPuerto;
import com.plazoleta.pedido.domain.spi.RestaurantesRemotosPuerto;

/**
 * Pruebas unitarias del caso de uso HU18 (eficiencia de los pedidos).
 */
@ExtendWith(MockitoExtension.class)
class MedidorEficienciaTest {

    private static final Long ID_RESTAURANTE = 1L;
    private static final Long ID_PROPIETARIO = 2L;
    private static final List<LineaPedido> LINEAS = List.of(new LineaPedido(1L, 2));

    @Mock
    private RepositorioPedidosPuerto repositorioPedidos;

    @Mock
    private RestaurantesRemotosPuerto restaurantes;

    private MedidorEficiencia medidor;

    @BeforeEach
    void preparar() {
        medidor = new MedidorEficiencia(repositorioPedidos, restaurantes);
    }

    private Pedido entregado(Long id, Long idEmpleado, int minutos) {
        return Pedido.reconstruir(id, 4L, ID_RESTAURANTE, EstadoPedido.ENTREGADO,
                LINEAS, idEmpleado, "hash", Instant.now().minusSeconds(3600),
                Instant.now().minusSeconds(3600 - minutos * 60L));
    }

    @Test
    void debeArmarElReporteConDuracionesYRankingParaElDueno() {
        when(restaurantes.consultarRestaurante(ID_RESTAURANTE))
                .thenReturn(new RestauranteRemoto(ID_RESTAURANTE, ID_PROPIETARIO));
        when(repositorioPedidos.buscarEntregadosDe(ID_RESTAURANTE)).thenReturn(List.of(
                entregado(10L, 5L, 30),
                entregado(11L, 5L, 20),
                entregado(12L, 6L, 45)));

        ReporteEficiencia reporte = medidor.ejecutar(ID_RESTAURANTE, ID_PROPIETARIO);

        assertEquals(3, reporte.pedidos().size());
        assertEquals(30, reporte.pedidos().get(0).duracionMinutos());
        assertEquals(2, reporte.rankingEmpleados().size());
        // El empleado 5 promedia 25 minutos y queda primero del ranking.
        assertEquals(5L, reporte.rankingEmpleados().get(0).idEmpleado());
        assertEquals(25.0, reporte.rankingEmpleados().get(0).promedioMinutos());
        assertEquals(2, reporte.rankingEmpleados().get(0).pedidosEntregados());
    }

    @Test
    void debeRechazarElReporteParaQuienNoEsElDueno() {
        when(restaurantes.consultarRestaurante(ID_RESTAURANTE))
                .thenReturn(new RestauranteRemoto(ID_RESTAURANTE, ID_PROPIETARIO));

        assertThatThrownBy(() -> medidor.ejecutar(ID_RESTAURANTE, 3L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("dueño");
    }

    @Test
    void debeEntregarUnReporteVacioSinPedidosEntregados() {
        when(restaurantes.consultarRestaurante(ID_RESTAURANTE))
                .thenReturn(new RestauranteRemoto(ID_RESTAURANTE, ID_PROPIETARIO));
        when(repositorioPedidos.buscarEntregadosDe(ID_RESTAURANTE)).thenReturn(List.of());

        ReporteEficiencia reporte = medidor.ejecutar(ID_RESTAURANTE, ID_PROPIETARIO);

        assertEquals(0, reporte.pedidos().size());
        assertEquals(0, reporte.rankingEmpleados().size());
    }
}
