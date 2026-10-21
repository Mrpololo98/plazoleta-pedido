package com.plazoleta.pedido.infrastructure.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.plazoleta.pedido.application.dto.PeticionCrearPedido;
import com.plazoleta.pedido.application.dto.PeticionEntregarPedido;
import com.plazoleta.pedido.application.dto.RespuestaEficiencia;
import com.plazoleta.pedido.application.dto.RespuestaHojaPedidos;
import com.plazoleta.pedido.application.dto.RespuestaPedido;
import com.plazoleta.pedido.application.dto.RespuestaPedidoListo;
import com.plazoleta.pedido.application.dto.RespuestaTrazabilidad;
import com.plazoleta.pedido.domain.api.CancelarPedido;
import com.plazoleta.pedido.domain.api.ConsultarEficiencia;
import com.plazoleta.pedido.domain.api.ConsultarTrazabilidad;
import com.plazoleta.pedido.domain.api.CrearPedido;
import com.plazoleta.pedido.domain.api.EntregarPedido;
import com.plazoleta.pedido.domain.api.ListarPedidos;
import com.plazoleta.pedido.domain.api.MarcarPedidoListo;
import com.plazoleta.pedido.domain.api.TomarPedido;
import com.plazoleta.pedido.domain.model.EstadoPedido;
import com.plazoleta.pedido.domain.model.LineaPedido;
import com.plazoleta.pedido.domain.model.Pedido;
import com.plazoleta.pedido.infrastructure.security.PortadorAutenticado;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Endpoints del ciclo de vida de los pedidos: alta por el cliente (HU11),
 * lista por estado para el empleado (HU12), asignación y preparación
 * (HU13), pedido listo con pin y SMS (HU14), entrega verificada (HU15),
 * cancelación (HU16), trazabilidad (HU17) y eficiencia (HU18).
 *
 * La identidad del cliente o del empleado responsable sale del token,
 * nunca de la petición.
 */
@RestController
@RequestMapping("/pedidos")
@Tag(name = "Pedidos", description = "Ciclo de vida de los pedidos de la plazoleta")
public class PedidoControlador {

    private final CrearPedido crearPedido;
    private final ListarPedidos listarPedidos;
    private final TomarPedido tomarPedido;
    private final MarcarPedidoListo marcarPedidoListo;
    private final EntregarPedido entregarPedido;
    private final CancelarPedido cancelarPedido;
    private final ConsultarTrazabilidad consultarTrazabilidad;
    private final ConsultarEficiencia consultarEficiencia;
    private final ConversorPedidosRest conversor;

    public PedidoControlador(CrearPedido crearPedido,
                             ListarPedidos listarPedidos,
                             TomarPedido tomarPedido,
                             MarcarPedidoListo marcarPedidoListo,
                             EntregarPedido entregarPedido,
                             CancelarPedido cancelarPedido,
                             ConsultarTrazabilidad consultarTrazabilidad,
                             ConsultarEficiencia consultarEficiencia,
                             ConversorPedidosRest conversor) {
        this.crearPedido = crearPedido;
        this.listarPedidos = listarPedidos;
        this.tomarPedido = tomarPedido;
        this.marcarPedidoListo = marcarPedidoListo;
        this.entregarPedido = entregarPedido;
        this.cancelarPedido = cancelarPedido;
        this.consultarTrazabilidad = consultarTrazabilidad;
        this.consultarEficiencia = consultarEficiencia;
        this.conversor = conversor;
    }

    @Operation(summary = "Realiza un pedido",
            description = "El cliente arma su orden con los platos de un mismo restaurante y sus "
                    + "cantidades. El pedido nace PENDIENTE; si el cliente ya tiene otro en "
                    + "proceso, o algún plato es de otro restaurante o está inhabilitado, se "
                    + "rechaza con 400.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido recibido, queda pendiente"),
            @ApiResponse(responseCode = "400", description = "Alguna regla de negocio no se cumplió"),
            @ApiResponse(responseCode = "401", description = "Falta token de acceso"),
            @ApiResponse(responseCode = "403", description = "El rol del token no es CLIENTE"),
            @ApiResponse(responseCode = "404", description = "Algún plato no existe")
    })
    @PostMapping
    public ResponseEntity<RespuestaPedido> crear(
            @AuthenticationPrincipal PortadorAutenticado portador,
            @Valid @RequestBody PeticionCrearPedido peticion) {
        List<LineaPedido> lineas = peticion.platos().stream()
                .map(linea -> new LineaPedido(linea.idPlato(), linea.cantidad()))
                .toList();
        Pedido guardado = crearPedido.ejecutar(
                portador.idUsuario(), peticion.idRestaurante(), lineas);
        return ResponseEntity.status(HttpStatus.CREATED).body(conversor.aRespuesta(guardado));
    }

    @Operation(summary = "Lista los pedidos por estado",
            description = "El empleado consulta sus pedidos filtrados por estado y por hojas; "
                    + "solo ve los del restaurante al que pertenece su cuenta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hoja de pedidos"),
            @ApiResponse(responseCode = "400", description = "Paginación solicitada no válida"),
            @ApiResponse(responseCode = "401", description = "Falta token de acceso"),
            @ApiResponse(responseCode = "403", description = "El rol del token no es EMPLEADO"),
            @ApiResponse(responseCode = "404", description = "La cuenta no es empleado de ningún restaurante")
    })
    @GetMapping
    public ResponseEntity<RespuestaHojaPedidos> listar(
            @AuthenticationPrincipal PortadorAutenticado portador,
            @RequestParam EstadoPedido estado,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int cantidad) {
        return ResponseEntity.ok(conversor.aRespuestaHoja(
                listarPedidos.ejecutar(portador.idUsuario(), estado, pagina, cantidad)));
    }

    @Operation(summary = "Se asigna un pedido y lo pasa a preparación",
            description = "El empleado toma un pedido pendiente de su restaurante: queda como "
                    + "responsable y el pedido cambia a EN_PREPARACION.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido asignado y en preparación"),
            @ApiResponse(responseCode = "400", description = "Alguna regla de negocio no se cumplió"),
            @ApiResponse(responseCode = "401", description = "Falta token de acceso"),
            @ApiResponse(responseCode = "403", description = "El rol del token no es EMPLEADO"),
            @ApiResponse(responseCode = "404", description = "El pedido no existe")
    })
    @PatchMapping("/{id}/asignar")
    public ResponseEntity<RespuestaPedido> asignar(
            @AuthenticationPrincipal PortadorAutenticado portador,
            @PathVariable Long id) {
        Pedido asignado = tomarPedido.ejecutar(id, portador.idUsuario());
        return ResponseEntity.ok(conversor.aRespuesta(asignado));
    }

    @Operation(summary = "Notifica que el pedido está listo",
            description = "El pedido pasa a LISTO, se genera un pin de seguridad de seis dígitos "
                    + "y se notifica por SMS al teléfono del cliente. La respuesta incluye el pin "
                    + "para poder ejercitar la entrega en el entorno de demo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido listo y cliente notificado"),
            @ApiResponse(responseCode = "400", description = "Alguna regla de negocio no se cumplió"),
            @ApiResponse(responseCode = "401", description = "Falta token de acceso"),
            @ApiResponse(responseCode = "403", description = "El rol del token no es EMPLEADO"),
            @ApiResponse(responseCode = "404", description = "El pedido o el cliente no existe")
    })
    @PatchMapping("/{id}/listo")
    public ResponseEntity<RespuestaPedidoListo> marcarListo(
            @AuthenticationPrincipal PortadorAutenticado portador,
            @PathVariable Long id) {
        return ResponseEntity.ok(conversor.aRespuestaListo(
                marcarPedidoListo.ejecutar(id, portador.idUsuario())));
    }

    @Operation(summary = "Entrega un pedido con el pin de seguridad",
            description = "Solo un pedido LISTO puede entregarse, y solo con el pin que se le "
                    + "notificó al cliente por SMS. Con la entrega el ciclo queda cerrado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido entregado"),
            @ApiResponse(responseCode = "400", description = "Pin incorrecto o estado no válido"),
            @ApiResponse(responseCode = "401", description = "Falta token de acceso"),
            @ApiResponse(responseCode = "403", description = "El rol del token no es EMPLEADO"),
            @ApiResponse(responseCode = "404", description = "El pedido no existe")
    })
    @PatchMapping("/{id}/entregar")
    public ResponseEntity<RespuestaPedido> entregar(
            @AuthenticationPrincipal PortadorAutenticado portador,
            @PathVariable Long id,
            @Valid @RequestBody PeticionEntregarPedido peticion) {
        Pedido entregado = entregarPedido.ejecutar(id, portador.idUsuario(), peticion.pinSeguridad());
        return ResponseEntity.ok(conversor.aRespuesta(entregado));
    }

    @Operation(summary = "Cancela un pedido pendiente",
            description = "El cliente cancela una de sus órdenes; solo es posible si sigue "
                    + "pendiente, si ya entró en preparación se rechaza con el mensaje de la "
                    + "historia de usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido cancelado"),
            @ApiResponse(responseCode = "400", description = "El pedido ya está en preparación"),
            @ApiResponse(responseCode = "401", description = "Falta token de acceso"),
            @ApiResponse(responseCode = "403", description = "El rol del token no es CLIENTE"),
            @ApiResponse(responseCode = "404", description = "No existe un pedido tuyo con ese id")
    })
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<RespuestaPedido> cancelar(
            @AuthenticationPrincipal PortadorAutenticado portador,
            @PathVariable Long id) {
        Pedido cancelado = cancelarPedido.ejecutar(id, portador.idUsuario());
        return ResponseEntity.ok(conversor.aRespuesta(cancelado));
    }

    @Operation(summary = "Consulta la trazabilidad de un pedido",
            description = "Historial de cambios de estado de una orden; cada cliente solo puede "
                    + "ver el historial de sus propios pedidos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial del pedido"),
            @ApiResponse(responseCode = "401", description = "Falta token de acceso"),
            @ApiResponse(responseCode = "403", description = "El rol del token no es CLIENTE"),
            @ApiResponse(responseCode = "404", description = "No existe un pedido tuyo con ese id")
    })
    @GetMapping("/{id}/trazabilidad")
    public ResponseEntity<List<RespuestaTrazabilidad>> trazabilidad(
            @AuthenticationPrincipal PortadorAutenticado portador,
            @PathVariable Long id) {
        List<RespuestaTrazabilidad> historial = consultarTrazabilidad
                .ejecutar(id, portador.idUsuario())
                .stream()
                .map(conversor::aRespuestaTrazabilidad)
                .toList();
        return ResponseEntity.ok(historial);
    }

    @Operation(summary = "Consulta la eficiencia de los pedidos",
            description = "Reporte para el dueño: duración de cada pedido entregado y ranking "
                    + "de empleados por tiempo promedio de atención.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reporte de eficiencia"),
            @ApiResponse(responseCode = "400", description = "El solicitante no es el dueño"),
            @ApiResponse(responseCode = "401", description = "Falta token de acceso"),
            @ApiResponse(responseCode = "403", description = "El rol del token no es PROPIETARIO"),
            @ApiResponse(responseCode = "404", description = "El restaurante no existe")
    })
    @GetMapping("/eficiencia")
    public ResponseEntity<RespuestaEficiencia> eficiencia(
            @AuthenticationPrincipal PortadorAutenticado portador,
            @RequestParam Long idRestaurante) {
        return ResponseEntity.ok(conversor.aRespuestaEficiencia(
                consultarEficiencia.ejecutar(idRestaurante, portador.idUsuario())));
    }
}
