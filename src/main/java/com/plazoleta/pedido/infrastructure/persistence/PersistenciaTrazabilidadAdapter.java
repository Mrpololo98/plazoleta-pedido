package com.plazoleta.pedido.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.plazoleta.pedido.domain.model.RegistroTrazabilidad;
import com.plazoleta.pedido.domain.spi.RepositorioTrazabilidadPuerto;

/**
 * Adaptador de salida: implementa el puerto del historial de cambios de
 * estado sobre JPA. El mapeo es directo, sin conversor.
 */
@Component
public class PersistenciaTrazabilidadAdapter implements RepositorioTrazabilidadPuerto {

    private final RepositorioTrazabilidadJpa repositorioJpa;

    public PersistenciaTrazabilidadAdapter(RepositorioTrazabilidadJpa repositorioJpa) {
        this.repositorioJpa = repositorioJpa;
    }

    @Override
    public RegistroTrazabilidad registrar(RegistroTrazabilidad registro) {
        TrazabilidadEntidad entidad = repositorioJpa.save(new TrazabilidadEntidad(
                registro.idPedido(), registro.estadoAnterior(),
                registro.estadoNuevo(), registro.fecha()));
        return new RegistroTrazabilidad(
                entidad.getId(), entidad.getIdPedido(), entidad.getEstadoAnterior(),
                entidad.getEstadoNuevo(), entidad.getFecha());
    }

    @Override
    public List<RegistroTrazabilidad> listarPorPedido(Long idPedido) {
        return repositorioJpa.findByIdPedidoOrderByIdAsc(idPedido).stream()
                .map(entidad -> new RegistroTrazabilidad(
                        entidad.getId(), entidad.getIdPedido(), entidad.getEstadoAnterior(),
                        entidad.getEstadoNuevo(), entidad.getFecha()))
                .toList();
    }
}
