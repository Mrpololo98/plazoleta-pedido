package com.plazoleta.pedido;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Microservicio de pedidos de la plazoleta de comidas.
 *
 * Cubre las historias de usuario HU11 (realizar pedido), HU12 y HU13 (gestión
 * del empleado), HU14 y HU15 (pedido listo y entrega con pin de seguridad),
 * HU16 (cancelación), HU17 (trazabilidad) y HU18 (eficiencia), además de la
 * parte de la HU5 que valida los tokens emitidos por el microservicio de
 * usuarios.
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.plazoleta.pedido.infrastructure.integraciones")
public class PlazoletaPedidosApplication {

    public static void main(String[] argumentos) {
        SpringApplication.run(PlazoletaPedidosApplication.class, argumentos);
    }
}
