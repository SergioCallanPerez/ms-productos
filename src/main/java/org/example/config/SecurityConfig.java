package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeExchange(exchanges -> exchanges
                        // Permitir preflight requests
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .pathMatchers("/actuator/**").permitAll()

                        // Protegidas con scopes
                        .pathMatchers(HttpMethod.GET, "/api/productos").hasAuthority("SCOPE_productos_read")
                        .pathMatchers(HttpMethod.GET, "/api/productos/{id}").hasAuthority("SCOPE_productos_read")
                        .pathMatchers(HttpMethod.POST, "/api/productos").hasAuthority("SCOPE_productos_create")
                        .pathMatchers(HttpMethod.PUT, "/api/productos/{id}").hasAuthority("SCOPE_productos_update")
                        .pathMatchers(HttpMethod.DELETE, "/api/productos/{id}").hasAuthority("SCOPE_productos_delete")
                        .pathMatchers(HttpMethod.PATCH, "/api/productos/{stock}/stock").hasAuthority("SCOPE_productos_stock_update")

                        .anyExchange().authenticated()
                )
                // Activar JWT como Resource Server
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }
}
