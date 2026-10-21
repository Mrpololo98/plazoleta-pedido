package com.plazoleta.pedido.infrastructure.persistence;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.plazoleta.pedido.domain.model.EstadoPedido;

/**
 * Repositorio JPA de pedidos.
 */
public interface RepositorioPedidosJpa extends JpaRepository<PedidoEntidad, Long> {

    /** ¿El cliente tiene un pedido en alguno de los estados indicados? (HU11) */
    boolean existsByIdClienteAndEstadoIn(Long idCliente, Collection<EstadoPedido> estados);

    /** Pedidos de un restaurante en un estado, por hojas (HU12). */
    Page<PedidoEntidad> findByIdRestauranteAndEstado(Long idRestaurante,
                                                     EstadoPedido estado, Pageable pageable);

    /** Pedidos de un restaurante en un estado, del más antiguo al más nuevo. */
    List<PedidoEntidad> findByIdRestauranteAndEstadoOrderByIdAsc(Long idRestaurante,
                                                                  EstadoPedido estado);
}
