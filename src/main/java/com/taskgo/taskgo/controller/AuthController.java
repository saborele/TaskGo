package com.taskgo.taskgo.controller;

import com.taskgo.taskgo.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // Simular autenticación (reemplazar con validación real de usuarios más adelante)
        if ("user".equals(username) && "password".equals(password)) {
            return jwtUtil.generateToken(username);
        }
        throw new IllegalArgumentException("Credenciales inválidas");
    }
}