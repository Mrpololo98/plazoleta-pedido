package com.plazoleta.pedido.infrastructure.rest;

import java.util.List;

import org.springframework.stereotype.Component;

import com.plazoleta.pedido.application.dto.RespuestaEficiencia;
import com.plazoleta.pedido.application.dto.RespuestaHojaPedidos;
import com.plazoleta.pedido.application.dto.RespuestaPedido;
import com.plazoleta.pedido.application.dto.RespuestaPedidoListo;
import com.plazoleta.pedido.application.dto.RespuestaTrazabilidad;
import com.plazoleta.pedido.domain.model.HojaPedidos;
import com.plazoleta.pedido.domain.model.Pedido;
import com.plazoleta.pedido.domain.model.PedidoListo;
import com.plazoleta.pedido.domain.model.RegistroTrazabilidad;
import com.plazoleta.pedido.domain.model.ReporteEficiencia;

/**
 * Traduce entre el modelo de dominio de pedidos y los DTO de salida HTTP.
 * El dominio protege su máquina de estados, así que aquí solo se copia la
 * vista ya calculada.
 */
@Component
public class ConversorPedidosRest {

    public RespuestaPedido aRespuesta(Pedido pedido) {
        List<RespuestaPedido.LineaRespuesta> lineas = pedido.lineas().stream()
                .map(linea -> new RespuestaPedido.LineaRespuesta(linea.idPlato(), linea.cantidad()))
                .toList();
        return new RespuestaPedido(
                pedido.id(), pedido.idCliente(), pedido.idRestaurante(), pedido.estado(),
                lineas, pedido.idEmpleadoAsignado(), pedido.fechaCreacion(), pedido.fechaCierre());
    }

    public RespuestaPedidoListo aRespuestaListo(PedidoListo listo) {
        return new RespuestaPedidoListo(
                listo.pedido().id(), listo.pedido().estado(),
                listo.telefonoNotificado(), listo.pinSeguridad());
    }

    public RespuestaHojaPedidos aRespuestaHoja(HojaPedidos hoja) {
        List<RespuestaPedido> pedidos = hoja.contenido().stream()
                .map(this::aRespuesta)
                .toList();
        return new RespuestaHojaPedidos(pedidos, hoja.pagina(), hoja.cantidad(),
                hoja.totalElementos(), hoja.totalHojas());
    }

    public RespuestaTrazabilidad aRespuestaTrazabilidad(RegistroTrazabilidad registro) {
        return new RespuestaTrazabilidad(registro.id(), registro.idPedido(),
                registro.estadoAnterior(), registro.estadoNuevo(), registro.fecha());
    }

    public RespuestaEficiencia aRespuestaEficiencia(ReporteEficiencia reporte) {
        List<RespuestaEficiencia.ResumenPedido> pedidos = reporte.pedidos().stream()
                .map(resumen -> new RespuestaEficiencia.ResumenPedido(
                        resumen.idPedido(), resumen.idCliente(), resumen.idEmpleado(),
                        resumen.estado(), resumen.duracionMinutos()))
                .toList();
        List<RespuestaEficiencia.PuestoEmpleado> ranking = reporte.rankingEmpleados().stream()
                .map(puesto -> new RespuestaEficiencia.PuestoEmpleado(
                        puesto.idEmpleado(), puesto.pedidosEntregados(), puesto.promedioMinutos()))
                .toList();
        return new RespuestaEficiencia(reporte.idRestaurante(), pedidos, ranking);
    }
}
