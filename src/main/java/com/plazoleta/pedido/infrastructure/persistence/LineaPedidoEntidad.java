package com.plazoleta.pedido.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Tabla linea_pedido: cada plato escogido dentro de un pedido con su
 * cantidad (HU11).
 */
@Entity
@Table(name = "linea_pedido")
public class LineaPedidoEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_pedido", nullable = false, insertable = false, updatable = false)
    private Long idPedido;

    @Column(name = "id_plato", nullable = false)
    private Long idPlato;

    @Column(nullable = false)
    private Integer cantidad;

    public LineaPedidoEntidad() {
    }

    public LineaPedidoEntidad(Long idPlato, Integer cantidad) {
        this.idPlato = idPlato;
        this.cantidad = cantidad;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdPedido() {
        return idPedido;
    }

    public Long getIdPlato() {
        return idPlato;
    }

    public void setIdPlato(Long idPlato) {
        this.idPlato = idPlato;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
