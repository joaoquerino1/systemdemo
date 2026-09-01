package com.logistica.sistema.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // sem header ou sem o prefixo esperado: segue sem autenticar
        // (a rota vai barrar depois, no SecurityConfig, se precisar de auth)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (jwtService.tokenValido(token)) {
            String email = jwtService.extrairEmail(token);

            // so autentica se ainda nao houver autenticacao no contexto
            // (evita sobrescrever em requisicoes internas/forward)
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    var userDetails = usuarioDetailsService.loadUserByUsername(email);

                    var authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (UsernameNotFoundException e) {
                    // o token e valido, mas o usuario nao existe mais
                    // (ex: excluido apos o token ter sido emitido).
                    // Segue sem autenticar - a rota vai barrar como
                    // "nao autenticado" em vez de derrubar com 500.
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
