package com.taskgo.taskgo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF (no lo necesitamos para APIs REST)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Usar sesiones sin estado (típico para JWT)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll() // Permitir acceso público a todos los endpoints bajo /api/**
                        .requestMatchers("/h2-console/**").permitAll() // Permitir acceso a la consola de H2
                        .anyRequest().authenticated() // Cualquier otra solicitud requiere autenticación
                )
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())); // Permitir que la consola de H2 se muestre en un iframe

        return http.build();
    }
}