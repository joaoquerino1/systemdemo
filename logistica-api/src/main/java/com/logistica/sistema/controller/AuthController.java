package com.logistica.sistema.controller;

import com.logistica.sistema.dto.LoginRequest;
import com.logistica.sistema.dto.LoginResponse;
import com.logistica.sistema.exception.RegraNegocioException;
import com.logistica.sistema.security.JwtService;
import com.logistica.sistema.security.RateLimiterService;
import com.logistica.sistema.security.UsuarioDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RateLimiterService rateLimiter;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        // Rate limit: bloqueia apos N tentativas dentro da janela de tempo
        if (!rateLimiter.permitirTentativa(email)) {
            throw new RegraNegocioException(
                    "Numero maximo de tentativas atingido. Tente novamente mais tarde.");
        }

        try {
            // AuthenticationManager delega para o DaoAuthenticationProvider,
            // que usa o UsuarioDetailsService + PasswordEncoder para validar
            // a senha. Se as credenciais estiverem erradas, ele lanca
            // BadCredentialsException automaticamente.
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.senha()));

            var usuarioDetails = (UsuarioDetails) authentication.getPrincipal();
            var usuario = usuarioDetails.getUsuario();

            // Login bem-sucedido: reseta o contador de tentativas
            rateLimiter.resetar(email);

            String token = jwtService.gerarToken(
                    usuario.getEmail(), usuario.getRole().name(), usuario.getId());

            return new LoginResponse(token, usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getRole().name());
        } catch (BadCredentialsException e) {
            // Credenciais invalidas: a tentativa ja foi contabilizada acima
            throw e;
        }
    }
}
