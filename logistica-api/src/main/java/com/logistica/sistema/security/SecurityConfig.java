package com.logistica.sistema.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final Environment environment;
    private final String origensPermitidas;

    public SecurityConfig(
            UsuarioDetailsService usuarioDetailsService,
            PasswordEncoder passwordEncoder,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            Environment environment,
            @Value("${app.cors.origens-permitidas}") String origensPermitidas) {
        this.usuarioDetailsService = usuarioDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.environment = environment;
        this.origensPermitidas = origensPermitidas;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        boolean isProduction = Arrays.asList(environment.getActiveProfiles()).contains("production");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Return 401 instead of 403 for unauthenticated requests
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                "{\"timestamp\":\"" + java.time.LocalDateTime.now() +
                                "\",\"status\":401,\"erro\":\"Unauthorized\",\"mensagem\":\"Nao autenticado.\"}"
                            );
                        })
                )
                .authorizeHttpRequests(authReq -> {
                    authReq.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    authReq.requestMatchers("/api/auth/login").permitAll();
                    authReq.requestMatchers("/api/usuarios/bootstrap-admin").permitAll();
                    authReq.requestMatchers("/actuator/health").permitAll();

                    if (!isProduction) {
                        authReq.requestMatchers(
                                "/docs", "/docs/**",
                                "/v3/api-docs", "/v3/api-docs/**",
                                "/swagger-ui/**", "/webjars/**"
                        ).permitAll();
                    }

                    authReq.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = new java.util.ArrayList<>(List.of(origensPermitidas.split(",")));
        // Always allow local development origins
        if (!origins.contains("http://localhost:4200")) {
            origins.add("http://localhost:4200");
        }
        config.setAllowedOriginPatterns(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Content-Disposition"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
