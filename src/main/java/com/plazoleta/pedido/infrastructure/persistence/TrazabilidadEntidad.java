package com.plazoleta.pedido.infrastructure.persistence;

import java.time.Instant;

import com.plazoleta.pedido.domain.model.EstadoPedido;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Tabla trazabilidad_pedido: un registro por cada cambio de estado de un
 * pedido, con el estado anterior y el nuevo (HU17).
 */
@Entity
@Table(name = "trazabilidad_pedido")
public class TrazabilidadEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_pedido", nullable = false)
    private Long idPedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 20)
    private EstadoPedido estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private EstadoPedido estadoNuevo;

    @Column(name = "fecha", nullable = false)
    private Instant fecha;

    public TrazabilidadEntidad() {
    }

    public TrazabilidadEntidad(Long idPedido, EstadoPedido estadoAnterior,
                               EstadoPedido estadoNuevo, Instant fecha) {
        this.idPedido = idPedido;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.fecha = fecha;
    }

    public Long getId() {
        return id;
    }

    public Long getIdPedido() {
        return idPedido;
    }

    public EstadoPedido getEstadoAnterior() {
        return estadoAnterior;
    }

    public EstadoPedido getEstadoNuevo() {
        return estadoNuevo;
    }

    public Instant getFecha() {
        return fecha;
    }
}
