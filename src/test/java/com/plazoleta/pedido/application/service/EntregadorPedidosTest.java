package com.plazoleta.pedido.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import com.plazoleta.pedido.domain.exception.ReglaNegocioException;
import com.plazoleta.pedido.domain.model.EstadoPedido;
import com.plazoleta.pedido.domain.model.LineaPedido;
import com.plazoleta.pedido.domain.model.Pedido;
import com.plazoleta.pedido.domain.spi.CifradorPinPuerto;
import com.plazoleta.pedido.domain.spi.RepositorioPedidosPuerto;

/**
 * Pruebas unitarias del caso de uso HU15 (entrega con pin de seguridad).
 */
@ExtendWith(MockitoExtension.class)
class EntregadorPedidosTest {

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
    private CifradorPinPuerto cifrador;

    private EntregadorPedidos entregador;

    @BeforeEach
    void preparar() {
        entregador = new EntregadorPedidos(repositorioPedidos, trazabilidad,
                verificadorAmbito, cifrador);
    }

    private Pedido pedidoListo() {
        return Pedido.reconstruir(10L, 4L, ID_RESTAURANTE, EstadoPedido.LISTO,
                LINEAS, ID_EMPLEADO, "hash-del-pin", Instant.now(), null);
    }

    @Test
    void debeCerrarElCicloCuandoElPinCoincide() {
        Pedido listo = pedidoListo();
        when(trazabilidad.obtenerPedido(10L)).thenReturn(listo);
        when(verificadorAmbito.restauranteDelEmpleado(ID_EMPLEADO)).thenReturn(ID_RESTAURANTE);
        when(cifrador.coincide("482913", "hash-del-pin")).thenReturn(true);
        when(repositorioPedidos.guardar(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        Pedido entregado = entregador.ejecutar(10L, ID_EMPLEADO, "482913");

        assertEquals(EstadoPedido.ENTREGADO, entregado.estado());
        assertNotNull(entregado.fechaCierre());
        verify(trazabilidad).registrar(entregado, EstadoPedido.LISTO, EstadoPedido.ENTREGADO);
    }

    @Test
    void debeRechazarLaEntregaConUnPinIncorrecto() {
        when(trazabilidad.obtenerPedido(10L)).thenReturn(pedidoListo());
        when(verificadorAmbito.restauranteDelEmpleado(ID_EMPLEADO)).thenReturn(ID_RESTAURANTE);
        when(cifrador.coincide("000000", "hash-del-pin")).thenReturn(false);

        assertThatThrownBy(() -> entregador.ejecutar(10L, ID_EMPLEADO, "000000"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no coincide");
    }

    @Test
    void debeRechazarEntregarUnPedidoQueNoEstaListo() {
        Pedido enPreparacion = Pedido.reconstruir(10L, 4L, ID_RESTAURANTE,
                EstadoPedido.EN_PREPARACION, LINEAS, ID_EMPLEADO, null, Instant.now(), null);
        when(trazabilidad.obtenerPedido(10L)).thenReturn(enPreparacion);
        when(verificadorAmbito.restauranteDelEmpleado(ID_EMPLEADO)).thenReturn(ID_RESTAURANTE);

        assertThatThrownBy(() -> entregador.ejecutar(10L, ID_EMPLEADO, "482913"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("pin de seguridad");
    }
}
