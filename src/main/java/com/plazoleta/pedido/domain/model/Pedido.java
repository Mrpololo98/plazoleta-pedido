package com.plazoleta.pedido.domain.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.plazoleta.pedido.domain.exception.ReglaNegocioException;

/**
 * Modelo de dominio de un pedido de la plazoleta de comidas.
 *
 * Es una pequeña máquina de estados: las transiciones permitidas son
 * PENDIENTE → EN_PREPARACION → LISTO → ENTREGADO, más PENDIENTE →
 * CANCELADO. Cada método de transición valida el estado actual y rechaza
 * con un mensaje claro cualquier salto ilegal, así ninguna capa externa
 * puede dejar el pedido en un estado inconsistente.
 */
public class Pedido {

    public static final String MENSAJE_CANCELACION_INVALIDA =
            "Lo sentimos, tu pedido ya está en preparación y no puede cancelarse";

    private Long id;
    private final Long idCliente;
    private final Long idRestaurante;
    private EstadoPedido estado;
    private final List<LineaPedido> lineas;
    private Long idEmpleadoAsignado;
    private String pinSeguridadHash;
    private final Instant fechaCreacion;
    private Instant fechaCierre;

    public Pedido() {
        this.id = null;
        this.idCliente = null;
        this.idRestaurante = null;
        this.estado = null;
        this.lineas = List.of();
        this.fechaCreacion = null;
    }

    private Pedido(Long id, Long idCliente, Long idRestaurante, EstadoPedido estado,
                   List<LineaPedido> lineas, Long idEmpleadoAsignado, String pinSeguridadHash,
                   Instant fechaCreacion, Instant fechaCierre) {
        this.id = id;
        this.idCliente = idCliente;
        this.idRestaurante = idRestaurante;
        this.estado = estado;
        this.lineas = List.copyOf(lineas);
        this.idEmpleadoAsignado = idEmpleadoAsignado;
        this.pinSeguridadHash = pinSeguridadHash;
        this.fechaCreacion = fechaCreacion;
        this.fechaCierre = fechaCierre;
    }

    /**
     * Pedido nuevo (HU11): nace pendiente y con la fecha de inicio del ciclo.
     */
    public static Pedido crear(Long idCliente, Long idRestaurante, List<LineaPedido> lineas) {
        return new Pedido(null, idCliente, idRestaurante, EstadoPedido.PENDIENTE,
                lineas, null, null, Instant.now(), null);
    }

    /**
     * Reconstruye un pedido ya guardado (lo usa el adaptador de persistencia).
     */
    public static Pedido reconstruir(Long id, Long idCliente, Long idRestaurante,
                                     EstadoPedido estado, List<LineaPedido> lineas,
                                     Long idEmpleadoAsignado, String pinSeguridadHash,
                                     Instant fechaCreacion, Instant fechaCierre) {
        return new Pedido(id, idCliente, idRestaurante, estado, lineas,
                idEmpleadoAsignado, pinSeguridadHash, fechaCreacion, fechaCierre);
    }

    /**
     * HU13: el empleado se asigna el pedido y este pasa a preparación.
     */
    public void asignarEmpleado(Long idEmpleado) {
        exigirEstado(EstadoPedido.PENDIENTE,
                "Solo los pedidos pendientes pueden pasar a preparación");
        this.idEmpleadoAsignado = idEmpleado;
        this.estado = EstadoPedido.EN_PREPARACION;
    }

    /**
     * HU14: la cocina termina el pedido y queda listo para reclamar.
     */
    public void marcarListo() {
        exigirEstado(EstadoPedido.EN_PREPARACION,
                "Solo los pedidos en preparación pueden marcarse como listos");
        this.estado = EstadoPedido.LISTO;
    }

    /**
     * Guarda el hash del pin de seguridad que se notificó al cliente (HU14).
     */
    public void fijarPinSeguridad(String pinSeguridadHash) {
        this.pinSeguridadHash = pinSeguridadHash;
    }

    /**
     * HU15: entrega final; cierra el ciclo y deja la fecha de fin.
     */
    public void entregar() {
        exigirEstado(EstadoPedido.LISTO, "Solo los pedidos listos pueden entregarse");
        this.estado = EstadoPedido.ENTREGADO;
        this.fechaCierre = Instant.now();
    }

    /**
     * HU16: solo un pedido pendiente puede cancelarse; el mensaje de rechazo
     * es el que pide la historia de usuario, textual.
     */
    public void cancelar() {
        if (this.estado != EstadoPedido.PENDIENTE) {
            throw new ReglaNegocioException(MENSAJE_CANCELACION_INVALIDA);
        }
        this.estado = EstadoPedido.CANCELADO;
        this.fechaCierre = Instant.now();
    }

    private void exigirEstado(EstadoPedido esperado, String mensajeDeRechazo) {
        if (this.estado != esperado) {
            throw new ReglaNegocioException(mensajeDeRechazo);
        }
    }

    /**
     * Minutos entre el inicio del pedido y su cierre (HU18). Solo aplica a
     * pedidos ya terminados (entregados o cancelados).
     */
    public long duracionMinutos() {
        if (fechaCierre == null) {
            throw new ReglaNegocioException(
                    "El pedido " + id + " aún no termina, no tiene duración");
        }
        return ChronoUnit.MINUTES.between(fechaCreacion, fechaCierre);
    }

    public Long id() {
        return id;
    }

    public Long idCliente() {
        return idCliente;
    }

    public Long idRestaurante() {
        return idRestaurante;
    }

    public EstadoPedido estado() {
        return estado;
    }

    public List<LineaPedido> lineas() {
        return lineas;
    }

    public Long idEmpleadoAsignado() {
        return idEmpleadoAsignado;
    }

    public String pinSeguridadHash() {
        return pinSeguridadHash;
    }

    public Instant fechaCreacion() {
        return fechaCreacion;
    }

    public Instant fechaCierre() {
        return fechaCierre;
    }
}
