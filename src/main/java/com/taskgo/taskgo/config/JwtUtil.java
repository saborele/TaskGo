package com.taskgo.taskgo.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hora

    public String generateToken(String username) {
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
        System.out.println("Token generado para " + username + ": " + token);
        return token;
    }

    public String extractUsername(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            System.out.println("Claims extraídos: " + claims);
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("El token ha expirado: " + e.getMessage());
        } catch (MalformedJwtException e) {
            throw new RuntimeException("El token está mal formado: " + e.getMessage());
        } catch (SignatureException e) {
            throw new RuntimeException("La firma del token es inválida: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            throw new RuntimeException("El token no es soportado: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error al parsear el token: " + e.getMessage());
        }
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            boolean isNotExpired = !claims.getExpiration().before(new Date());
            System.out.println("Validando token - No expirado: " + isNotExpired + ", Claims: " + claims);
            return isNotExpired;
        } catch (ExpiredJwtException e) {
            System.out.println("El token ha expirado: " + e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            System.out.println("El token está mal formado: " + e.getMessage());
            return false;
        } catch (SignatureException e) {
            System.out.println("La firma del token es inválida: " + e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            System.out.println("El token no es soportado: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("Error al validar el token: " + e.getMessage());
            return false;
        }
    }
}