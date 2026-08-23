package com.bloodbridge.config;

import com.bloodbridge.security.JwtAuthenticationEntryPoint;
import com.bloodbridge.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Main security configuration class for the application.
 * Configures JWT-based stateless authentication, CORS, and endpoint access rules.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final AuthenticationProvider authenticationProvider;

    @org.springframework.beans.factory.annotation.Value("${app.cors.allowed-origins:https://blood-bridge-sepia.vercel.app,http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173,http://localhost:8083}")
    private String allowedOrigins;

    @org.springframework.beans.factory.annotation.Value("${app.frontend.url:https://blood-bridge-sepia.vercel.app}")
    private String frontendUrl;

    /**
     * Configures the security filter chain.
     *
     * @param http the HttpSecurity configuration object
     * @return the security filter chain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .authorizeHttpRequests(auth -> auth
                        // Permit all CORS preflight OPTIONS requests before authentication check
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/auth/**",
                                "/api/v1/debug/**",
                                "/api/debug/**",
                                "/api/v1/admin/emergency-stats/**",
                                "/api/admin/emergency-stats/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/ws/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers("/api/v1/admin/**", "/api/admin/**").hasAnyRole("ADMIN", "HOSPITAL")
                        .requestMatchers("/api/v1/donor/**", "/api/donor/**", "/api/v1/donors/**", "/api/donors/**").hasAnyRole("DONOR", "ADMIN")
                        .requestMatchers("/api/v1/hospital/**", "/api/hospital/**", "/api/v1/hospitals/**", "/api/hospitals/**").hasAnyRole("HOSPITAL", "ADMIN")
                        .requestMatchers("/api/v1/location/**").authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures CORS using configured origin patterns from properties and environment.
     * Supports credentials for SockJS WebSocket handshake and REST API interactions.
     *
     * @return the CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        Set<String> originPatterns = new LinkedHashSet<>();
        
        // 1. Explicit production frontend origins
        originPatterns.add("https://blood-bridge-sepia.vercel.app");
        originPatterns.add("https://*.vercel.app");

        // 2. Local development origins
        originPatterns.add("http://localhost:5173");
        originPatterns.add("http://localhost:3000");
        originPatterns.add("http://127.0.0.1:5173");
        originPatterns.add("http://localhost:8083");

        // 3. Dynamic origin patterns from environment properties
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            for (String origin : allowedOrigins.split(",")) {
                String trimmed = origin.trim();
                if (!trimmed.isEmpty()) {
                    originPatterns.add(trimmed);
                }
            }
        }
        if (frontendUrl != null && !frontendUrl.isBlank()) {
            originPatterns.add(frontendUrl.trim());
        }

        configuration.setAllowedOriginPatterns(new ArrayList<>(originPatterns));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-API-KEY",
                "Accept",
                "Origin",
                "X-Requested-With",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
