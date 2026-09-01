package com.logistica.sistema.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiter simples em memoria para o endpoint de login.
 * Implementa uma janela deslizante por email: apos N tentativas
 * dentro de um periodo de T segundos, o login e bloqueado temporariamente.
 *
 * Em producao com multiplas instancias, considerar usar Redis ou
 * um rate limiter distribuido (ex: Bucket4j).
 */
@Service
public class RateLimiterService {

    private final int maxTentativas;
    private final long janelaSegundos;
    private final ConcurrentHashMap<String, TentativaLogin> tentativas = new ConcurrentHashMap<>();

    public RateLimiterService(
            @Value("${app.ratelimit.login-max-tentativas:5}") int maxTentativas,
            @Value("${app.ratelimit.login-janela-segundos:300}") long janelaSegundos) {
        this.maxTentativas = maxTentativas;
        this.janelaSegundos = janelaSegundos;
    }

    /**
     * Registra uma tentativa de login para o email informado.
     * @return true se a tentativa e permitida, false se o usuario esta bloqueado
     */
    public boolean permitirTentativa(String email) {
        String chave = email.toLowerCase().trim();
        long agora = System.currentTimeMillis();
        long janelaMs = janelaSegundos * 1000;

        TentativaLogin tentativa = tentativas.compute(chave, (k, atual) -> {
            if (atual == null || (agora - atual.primeiraTentativa) > janelaMs) {
                // Nova janela
                return new TentativaLogin(agora, new AtomicInteger(1));
            }
            atual.contador.incrementAndGet();
            return atual;
        });

        return tentativa.contador.get() <= maxTentativas;
    }

    /**
     * Retorna o numero de tentativas restantes antes do bloqueio.
     */
    public int tentativasRestantes(String email) {
        String chave = email.toLowerCase().trim();
        TentativaLogin tentativa = tentativas.get(chave);
        if (tentativa == null) {
            return maxTentativas;
        }
        return Math.max(0, maxTentativas - tentativa.contador.get());
    }

    /**
     * Limpa o registro de tentativas (chamar apos login bem-sucedido).
     */
    public void resetar(String email) {
        tentativas.remove(email.toLowerCase().trim());
    }

    private static class TentativaLogin {
        final long primeiraTentativa;
        final AtomicInteger contador;

        TentativaLogin(long primeiraTentativa, AtomicInteger contador) {
            this.primeiraTentativa = primeiraTentativa;
            this.contador = contador;
        }
    }
}
