package com.plazoleta.pedido.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.plazoleta.pedido.domain.model.EstadoPedido;
import com.plazoleta.pedido.domain.model.HojaPedidos;
import com.plazoleta.pedido.domain.model.LineaPedido;
import com.plazoleta.pedido.domain.model.Pedido;
import com.plazoleta.pedido.domain.spi.RepositorioPedidosPuerto;

/**
 * Adaptador de salida: implementa el puerto de pedidos sobre JPA. El
 * mapeo entre la entidad y el dominio se hace a mano porque las líneas del
 * pedido viajan dentro de la entidad y el dominio las mantiene inmutables.
 * Los métodos son transaccionales para que el mapeo de las líneas suceda
 * con la sesión abierta (la aplicación corre con open-in-view desactivado).
 */
@Component
public class PersistenciaPedidosAdapter implements RepositorioPedidosPuerto {

    private final RepositorioPedidosJpa repositorioJpa;

    public PersistenciaPedidosAdapter(RepositorioPedidosJpa repositorioJpa) {
        this.repositorioJpa = repositorioJpa;
    }

    @Override
    @Transactional
    public Pedido guardar(Pedido pedido) {
        PedidoEntidad entidad = repositorioJpa.save(aEntidad(pedido));
        return aDominio(entidad);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pedido> obtenerPorId(Long id) {
        return repositorioJpa.findById(id).map(this::aDominio);
    }

    @Override
    public boolean existePedidoActivoDe(Long idCliente) {
        return repositorioJpa.existsByIdClienteAndEstadoIn(
                idCliente, List.of(EstadoPedido.EN_PROCESO));
    }

    @Override
    @Transactional(readOnly = true)
    public HojaPedidos buscarPorRestauranteYEstado(Long idRestaurante, EstadoPedido estado,
                                                   int pagina, int cantidad) {
        Pageable hoja = PageRequest.of(pagina, cantidad,
                Sort.by("fechaCreacion").ascending());
        Page<PedidoEntidad> resultados = repositorioJpa.findByIdRestauranteAndEstado(
                idRestaurante, estado, hoja);
        return new HojaPedidos(
                resultados.getContent().stream().map(this::aDominio).toList(),
                resultados.getNumber(),
                resultados.getSize(),
                resultados.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> buscarEntregadosDe(Long idRestaurante) {
        return repositorioJpa
                .findByIdRestauranteAndEstadoOrderByIdAsc(idRestaurante, EstadoPedido.ENTREGADO)
                .stream()
                .map(this::aDominio)
                .toList();
    }

    private PedidoEntidad aEntidad(Pedido pedido) {
        PedidoEntidad entidad = new PedidoEntidad();
        entidad.setId(pedido.id());
        entidad.setIdCliente(pedido.idCliente());
        entidad.setIdRestaurante(pedido.idRestaurante());
        entidad.setEstado(pedido.estado());
        entidad.setIdEmpleadoAsignado(pedido.idEmpleadoAsignado());
        entidad.setPinSeguridadHash(pedido.pinSeguridadHash());
        entidad.setFechaCreacion(pedido.fechaCreacion());
        entidad.setFechaCierre(pedido.fechaCierre());
        entidad.setLineas(pedido.lineas().stream()
                .map(linea -> new LineaPedidoEntidad(linea.idPlato(), linea.cantidad()))
                .toList());
        return entidad;
    }

    private Pedido aDominio(PedidoEntidad entidad) {
        List<LineaPedido> lineas = entidad.getLineas().stream()
                .map(linea -> new LineaPedido(linea.getIdPlato(), linea.getCantidad()))
                .toList();
        return Pedido.reconstruir(
                entidad.getId(),
                entidad.getIdCliente(),
                entidad.getIdRestaurante(),
                entidad.getEstado(),
                lineas,
                entidad.getIdEmpleadoAsignado(),
                entidad.getPinSeguridadHash(),
                entidad.getFechaCreacion(),
                entidad.getFechaCierre());
    }
}
