package com.plazoleta.pedido.infrastructure.security;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Seguridad HTTP del microservicio de pedidos (parte de la HU5).
 *
 * - POST  /pedidos                      → solo CLIENTE (HU11)
 * - GET   /pedidos                      → solo EMPLEADO (HU12)
 * - PATCH /pedidos/{id}/asignar         → solo EMPLEADO (HU13)
 * - PATCH /pedidos/{id}/listo           → solo EMPLEADO (HU14)
 * - PATCH /pedidos/{id}/entregar        → solo EMPLEADO (HU15)
 * - PATCH /pedidos/{id}/cancelar        → solo CLIENTE (HU16)
 * - GET   /pedidos/{id}/trazabilidad    → solo CLIENTE (HU17)
 * - GET   /pedidos/eficiencia           → solo PROPIETARIO (HU18)
 * - Swagger y /error                    → público
 *
 * La API no guarda sesión: cada petición viaja con su JWT.
 */
@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {

    @Bean
    public SecurityFilterChain cadenaSeguridad(HttpSecurity http,
                                               PuntoEntradaNoAutorizado puntoEntrada,
                                               @Value("${seguridad.jwt.secreto}") String secreto)
            throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(errores -> errores
                    .authenticationEntryPoint(puntoEntrada)
                    .accessDeniedHandler((peticion, respuesta, denegado) -> {
                        respuesta.setStatus(HttpStatus.FORBIDDEN.value());
                        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        respuesta.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
                        respuesta.getWriter().write(
                                "{\"estado\":403,\"mensaje\":\"Su rol no tiene permiso para esta operación\""
                                        + ",\"ocurridoEn\":\"" + Instant.now() + "\"}");
                    }))
            .authorizeHttpRequests(reglas -> reglas
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/error")
                        .permitAll()
                    .requestMatchers(HttpMethod.POST, "/pedidos").hasRole("CLIENTE")
                    .requestMatchers(HttpMethod.GET, "/pedidos").hasRole("EMPLEADO")
                    .requestMatchers(HttpMethod.PATCH, "/pedidos/*/asignar").hasRole("EMPLEADO")
                    .requestMatchers(HttpMethod.PATCH, "/pedidos/*/listo").hasRole("EMPLEADO")
                    .requestMatchers(HttpMethod.PATCH, "/pedidos/*/entregar").hasRole("EMPLEADO")
                    .requestMatchers(HttpMethod.PATCH, "/pedidos/*/cancelar").hasRole("CLIENTE")
                    .requestMatchers(HttpMethod.GET, "/pedidos/*/trazabilidad").hasRole("CLIENTE")
                    .requestMatchers(HttpMethod.GET, "/pedidos/eficiencia").hasRole("PROPIETARIO")
                    .anyRequest().authenticated())
            .addFilterBefore(new FiltroTokenJwt(secreto), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
