package com.bloodbridge.config;

import com.bloodbridge.security.JwtAuthenticationEntryPoint;
import com.bloodbridge.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import java.util.List;

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

    @org.springframework.beans.factory.annotation.Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173,http://localhost:8083}")
    private String allowedOrigins;

    @org.springframework.beans.factory.annotation.Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Configures CORS using configured origin patterns from properties and environment.
     * Supports credentials for SockJS WebSocket handshake and REST API interactions.
     *
     * @return the CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        java.util.Set<String> originPatterns = new java.util.LinkedHashSet<>();
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
        if (originPatterns.isEmpty()) {
            originPatterns.add("*");
        }

        configuration.setAllowedOriginPatterns(new java.util.ArrayList<>(originPatterns));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
