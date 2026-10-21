package com.plazoleta.pedido.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA del historial de cambios de estado (HU17).
 */
public interface RepositorioTrazabilidadJpa extends JpaRepository<TrazabilidadEntidad, Long> {

    List<TrazabilidadEntidad> findByIdPedidoOrderByIdAsc(Long idPedido);
}
