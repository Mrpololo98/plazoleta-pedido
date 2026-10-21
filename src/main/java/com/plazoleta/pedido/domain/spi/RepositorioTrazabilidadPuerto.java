package com.plazoleta.pedido.domain.spi;

import java.util.List;

import com.plazoleta.pedido.domain.model.RegistroTrazabilidad;

/**
 * Puerto de salida para el historial de cambios de estado de cada pedido
 * (HU17).
 */
public interface RepositorioTrazabilidadPuerto {

    RegistroTrazabilidad registrar(RegistroTrazabilidad registro);

    List<RegistroTrazabilidad> listarPorPedido(Long idPedido);
}
