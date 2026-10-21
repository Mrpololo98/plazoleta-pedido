package com.plazoleta.pedido.application.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.plazoleta.pedido.domain.api.ConsultarEficiencia;
import com.plazoleta.pedido.domain.exception.ReglaNegocioException;
import com.plazoleta.pedido.domain.model.Pedido;
import com.plazoleta.pedido.domain.model.ReporteEficiencia;
import com.plazoleta.pedido.domain.model.RestauranteRemoto;
import com.plazoleta.pedido.domain.spi.RepositorioPedidosPuerto;
import com.plazoleta.pedido.domain.spi.RestaurantesRemotosPuerto;

/**
 * Implementación HU18: eficiencia de los pedidos de un restaurante.
 *
 * De cada pedido entregado muestra el tiempo entre su inicio y su fin, y
 * arma el ranking de empleados por tiempo promedio de atención. Solo el
 * dueño del establecimiento puede pedir el reporte.
 */
@Service
public class MedidorEficiencia implements ConsultarEficiencia {

    private final RepositorioPedidosPuerto repositorioPedidos;
    private final RestaurantesRemotosPuerto restaurantes;

    public MedidorEficiencia(RepositorioPedidosPuerto repositorioPedidos,
                             RestaurantesRemotosPuerto restaurantes) {
        this.repositorioPedidos = repositorioPedidos;
        this.restaurantes = restaurantes;
    }

    @Override
    public ReporteEficiencia ejecutar(Long idRestaurante, Long idPropietario) {
        RestauranteRemoto restaurante = restaurantes.consultarRestaurante(idRestaurante);
        if (!restaurante.idPropietario().equals(idPropietario)) {
            throw new ReglaNegocioException(
                    "Solo el dueño del restaurante puede consultar su eficiencia");
        }

        List<Pedido> entregados = repositorioPedidos.buscarEntregadosDe(idRestaurante);

        List<ReporteEficiencia.ResumenPedido> resumenes = entregados.stream()
                .map(pedido -> new ReporteEficiencia.ResumenPedido(
                        pedido.id(), pedido.idCliente(), pedido.idEmpleadoAsignado(),
                        pedido.estado(), pedido.duracionMinutos()))
                .toList();

        return new ReporteEficiencia(idRestaurante, resumenes, ranking(entregados));
    }

    private List<ReporteEficiencia.PuestoEmpleado> ranking(List<Pedido> entregados) {
        Map<Long, List<Pedido>> porEmpleado = entregados.stream()
                .filter(pedido -> pedido.idEmpleadoAsignado() != null)
                .collect(Collectors.groupingBy(Pedido::idEmpleadoAsignado));

        return porEmpleado.entrySet().stream()
                .map(entrada -> {
                    List<Long> duraciones = entrada.getValue().stream()
                            .map(Pedido::duracionMinutos)
                            .toList();
                    double promedio = duraciones.stream()
                            .mapToLong(Long::longValue)
                            .average()
                            .orElse(0.0);
                    return new ReporteEficiencia.PuestoEmpleado(
                            entrada.getKey(), entrada.getValue().size(), promedio);
                })
                .sorted(Comparator.comparingDouble(ReporteEficiencia.PuestoEmpleado::promedioMinutos))
                .toList();
    }
}
