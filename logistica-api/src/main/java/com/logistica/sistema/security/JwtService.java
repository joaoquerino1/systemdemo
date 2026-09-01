package com.logistica.sistema.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoHoras;

    public JwtService(
            @Value("${app.jwt.secret}") String segredo,
            @Value("${app.jwt.expiracao-horas}") long expiracaoHoras) {
        // A chave precisa ter pelo menos 256 bits para HS256; em producao,
        // configure app.jwt.secret via variavel de ambiente com uma
        // string longa e aleatoria (nao usar o valor padrao do application.yml)
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes());
        this.expiracaoHoras = expiracaoHoras;
    }

    public String gerarToken(String email, String role, Long usuarioId) {
        Instant agora = Instant.now();
        Instant expiracao = agora.plus(expiracaoHoras, ChronoUnit.HOURS);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("usuarioId", usuarioId)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiracao))
                .signWith(chave)
                .compact();
    }

    public Claims validarEExtrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extrairEmail(String token) {
        return validarEExtrairClaims(token).getSubject();
    }

    public boolean tokenValido(String token) {
        try {
            Claims claims = validarEExtrairClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            // qualquer falha (token expirado, assinatura invalida, malformado)
            // significa que o token nao e valido
            return false;
        }
    }
}
