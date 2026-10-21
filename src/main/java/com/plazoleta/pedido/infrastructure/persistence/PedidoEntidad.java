package com.plazoleta.pedido.infrastructure.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.plazoleta.pedido.domain.model.EstadoPedido;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Tabla pedido: una orden de un cliente a un restaurante, con su estado
 * dentro del ciclo de vida (HU11 a HU16), el empleado responsable, el hash
 * del pin de seguridad y las fechas de inicio y cierre del ciclo.
 */
@Entity
@Table(name = "pedido")
public class PedidoEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @Column(name = "id_restaurante", nullable = false)
    private Long idRestaurante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado;

    @Column(name = "id_empleado_asignado")
    private Long idEmpleadoAsignado;

    @Column(name = "pin_seguridad_hash", length = 100)
    private String pinSeguridadHash;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_cierre")
    private Instant fechaCierre;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_pedido", nullable = false)
    private List<LineaPedidoEntidad> lineas = new ArrayList<>();

    public PedidoEntidad() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public Long getIdRestaurante() {
        return idRestaurante;
    }

    public void setIdRestaurante(Long idRestaurante) {
        this.idRestaurante = idRestaurante;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public Long getIdEmpleadoAsignado() {
        return idEmpleadoAsignado;
    }

    public void setIdEmpleadoAsignado(Long idEmpleadoAsignado) {
        this.idEmpleadoAsignado = idEmpleadoAsignado;
    }

    public String getPinSeguridadHash() {
        return pinSeguridadHash;
    }

    public void setPinSeguridadHash(String pinSeguridadHash) {
        this.pinSeguridadHash = pinSeguridadHash;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Instant getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(Instant fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public List<LineaPedidoEntidad> getLineas() {
        return lineas;
    }

    public void setLineas(List<LineaPedidoEntidad> lineas) {
        this.lineas = lineas;
    }
}
