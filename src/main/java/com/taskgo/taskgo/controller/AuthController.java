package com.taskgo.taskgo.controller;

import com.taskgo.taskgo.config.JwtUtil;
import com.taskgo.taskgo.model.Usuario;
import com.taskgo.taskgo.repository.UsuarioRepository;
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

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // Buscar el usuario en la base de datos
        Usuario usuario = usuarioRepository.findByUsername(username);
        if (usuario == null || !usuario.getPassword().equals(password)) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        return jwtUtil.generateToken(username);
    }
}