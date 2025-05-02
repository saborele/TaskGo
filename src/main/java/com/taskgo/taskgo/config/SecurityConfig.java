package com.taskgo.taskgo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/login", "/h2-console/**", "/", "/index.html", "/app.js", "/favicon.ico").permitAll()
                        .requestMatchers("/api/tareas/**").authenticated() // Proteger solo las rutas de tareas
                        .anyRequest().permitAll() // Permitir todo lo demás por ahora (ajustar según necesidades)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // Configuración para la consola H2
        http.headers().frameOptions().disable();

        return http.build();
    }
}