package com.plazoleta.pedido.infrastructure.integraciones;

import org.springframework.stereotype.Component;

import com.plazoleta.pedido.domain.exception.RecursoNoEncontradoException;
import com.plazoleta.pedido.domain.exception.ReglaNegocioException;
import com.plazoleta.pedido.domain.model.CuentaCliente;
import com.plazoleta.pedido.domain.spi.UsuariosRemotosPuerto;
import com.plazoleta.pedido.infrastructure.integraciones.remoto.CuentaUsuarioRemota;

import feign.FeignException;

/**
 * Adaptador de salida: cumple el puerto de usuarios remotos usando el
 * cliente Feign. Traduce los fallos HTTP del otro microservicio en las
 * excepciones de negocio de este dominio para que el flujo no filtre
 * detalles de transporte.
 */
@Component
public class AdaptadorUsuariosRemotos implements UsuariosRemotosPuerto {

    private final ClienteUsuariosApi clienteUsuarios;

    public AdaptadorUsuariosRemotos(ClienteUsuariosApi clienteUsuarios) {
        this.clienteUsuarios = clienteUsuarios;
    }

    @Override
    public CuentaCliente consultarCuenta(Long idCliente) {
        try {
            CuentaUsuarioRemota cuenta = clienteUsuarios.consultarCuenta(idCliente);
            return new CuentaCliente(cuenta.id(), cuenta.nombre(), cuenta.celular());
        } catch (FeignException.NotFound cuentaAusente) {
            throw new RecursoNoEncontradoException(
                    "No hay una cuenta de cliente con el identificador " + idCliente);
        } catch (FeignException servicioCaido) {
            throw new ReglaNegocioException(
                    "El servicio de usuarios no está disponible, intente de nuevo más tarde");
        }
    }
}
