package com.taskgo.taskgo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    private static final String[] PUBLIC_PATHS = {
            "/api/auth/login",
            "/h2-console/**",
            "/",
            "/index.html",
            "/app.js",
            "/favicon.ico"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        System.out.println("Procesando solicitud para: " + path + " con método: " + request.getMethod());

        boolean isPublic = false;
        for (String publicPath : PUBLIC_PATHS) {
            if (publicPath.endsWith("**")) {
                String basePath = publicPath.replace("**", "");
                if (path.startsWith(basePath)) {
                    isPublic = true;
                    break;
                }
            } else if (path.equals(publicPath)) {
                isPublic = true;
                break;
            }
        }

        if (isPublic) {
            System.out.println("Ruta pública detectada, ignorando filtro: " + path);
            chain.doFilter(request, response);
            return;
        }

        // Procesar rutas protegidas
        String authHeader = request.getHeader("Authorization");
        System.out.println("Encabezado Authorization recibido: " + authHeader);
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ") && authHeader.length() > 7) {
            token = authHeader.substring(7);
            System.out.println("Token extraído: " + token);
            try {
                username = jwtUtil.extractUsername(token);
                System.out.println("Username extraído del token: " + username);
            } catch (Exception e) {
                System.out.println("Error al extraer el username del token: " + e.getMessage());
            }
        } else {
            System.out.println("Encabezado Authorization inválido o ausente: " + authHeader);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(token)) {
                System.out.println("Token válido, configurando autenticación para: " + username);
                UserDetails userDetails = User.withUsername(username).password("").roles("USER").build();
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                System.out.println("Token inválido para el usuario: " + username);
            }
        } else {
            System.out.println("No se configuró la autenticación: username=" + username + ", auth=" + SecurityContextHolder.getContext().getAuthentication());
        }

        chain.doFilter(request, response);
    }
}