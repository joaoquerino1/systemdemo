package com.logistica.sistema.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Object> handleNaoEncontrado(RecursoNaoEncontradoException ex) {
        return corpoErro(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Object> handleRegraNegocio(RegraNegocioException ex) {
        return corpoErro(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleCredenciaisInvalidas(BadCredentialsException ex) {
        return corpoErro(HttpStatus.UNAUTHORIZED, "Email ou senha inválidos.");
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Object> handleUsuarioInativo(DisabledException ex) {
        return corpoErro(HttpStatus.UNAUTHORIZED, "Este usuário está inativo.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAcessoNegado(AccessDeniedException ex) {
        return corpoErro(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // Rede de seguranca para violacoes de constraint unica que escaparam
    // das checagens explicitas nos services (ex: condicao de corrida
    // entre duas requisicoes simultaneas). Sem isso, o erro vira um
    // 500 cru em vez de uma mensagem de negocio compreensivel.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleViolacaoDeIntegridade(DataIntegrityViolationException ex) {
        return corpoErro(HttpStatus.CONFLICT,
                "Este registro conflita com um dado já existente (ex: código, placa, email, CPF ou matrícula duplicados).");
    }

    // Lancada automaticamente pelo Spring quando um DTO com @Valid falha
    // na validacao (ex: @NotBlank, @Email, @PositiveOrZero). Sem este
    // handler, o erro ainda funciona, mas com um formato de resposta
    // diferente do resto da API - aqui deixamos consistente.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> camposComErro = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(erro ->
                camposComErro.put(erro.getField(), erro.getDefaultMessage()));

        Map<String, Object> corpo = new HashMap<>();
        corpo.put("timestamp", LocalDateTime.now().toString());
        corpo.put("status", HttpStatus.BAD_REQUEST.value());
        corpo.put("erro", "Dados inválidos");
        corpo.put("campos", camposComErro);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
    }

    private ResponseEntity<Object> corpoErro(HttpStatus status, String mensagem) {
        Map<String, Object> corpo = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.value(),
                "erro", status.getReasonPhrase(),
                "mensagem", mensagem
        );
        return ResponseEntity.status(status).body(corpo);
    }
}
