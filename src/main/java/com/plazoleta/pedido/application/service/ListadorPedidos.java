package com.plazoleta.pedido.application.service;

import org.springframework.stereotype.Service;

import com.plazoleta.pedido.domain.api.ListarPedidos;
import com.plazoleta.pedido.domain.exception.ReglaNegocioException;
import com.plazoleta.pedido.domain.model.EstadoPedido;
import com.plazoleta.pedido.domain.model.HojaPedidos;
import com.plazoleta.pedido.domain.spi.RepositorioPedidosPuerto;

/**
 * Implementación HU12: lista de pedidos filtrando por estado.
 *
 * El empleado solo ve los pedidos del restaurante al que pertenece (el
 * vínculo lo resuelve el microservicio de restaurantes) y la hoja se
 * pagina con la cantidad que él pida.
 */
@Service
public class ListadorPedidos implements ListarPedidos {

    private final RepositorioPedidosPuerto repositorioPedidos;
    private final VerificadorAmbitoEmpleado verificadorAmbito;

    public ListadorPedidos(RepositorioPedidosPuerto repositorioPedidos,
                           VerificadorAmbitoEmpleado verificadorAmbito) {
        this.repositorioPedidos = repositorioPedidos;
        this.verificadorAmbito = verificadorAmbito;
    }

    @Override
    public HojaPedidos ejecutar(Long idEmpleado, EstadoPedido estado, int pagina, int cantidad) {
        if (pagina < 0) {
            throw new ReglaNegocioException("El número de página solicitado no puede ser negativo");
        }
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad de pedidos por página debe ser mayor que cero");
        }
        Long idRestaurante = verificadorAmbito.restauranteDelEmpleado(idEmpleado);
        return repositorioPedidos.buscarPorRestauranteYEstado(idRestaurante, estado, pagina, cantidad);
    }
}
