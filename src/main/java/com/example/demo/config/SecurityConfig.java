package com.example.demo.config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()

                        // =========================
                        // PLANS
                        // =========================

                        // Customers need to see available plans
                        .requestMatchers(HttpMethod.GET, "/api/plans/**")
                        .authenticated()

                        // Only admins create plans
                        .requestMatchers(HttpMethod.POST, "/api/plans/**")
                        .hasRole("ADMIN")

                        // =========================
                        // ADMIN-ONLY "GET ALL"
                        // =========================

                        .requestMatchers(HttpMethod.GET, "/api/subscriptions")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/invoices")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/payment-attempts")
                        .hasRole("ADMIN")

                        // =========================
                        // EVERYTHING ELSE
                        // =========================

                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(this::jwtAuthenticationConverter)
                        )
                );

        return http.build();
    }

    private JwtAuthenticationToken jwtAuthenticationConverter(Jwt jwt) {

        String email = jwt.getClaimAsString("email");

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT does not contain an email claim"
            );
        }

        Map<String, Object> realmAccess =
                jwt.getClaim("realm_access");

        List<String> roles = Collections.emptyList();

        if (realmAccess != null) {
            Object rolesObject = realmAccess.get("roles");

            if (rolesObject instanceof List<?>) {
                roles = ((List<?>) rolesObject)
                        .stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .toList();
            }
        }

        Collection<SimpleGrantedAuthority> authorities =
                roles.stream()
                        .map(role ->
                                new SimpleGrantedAuthority(
                                        "ROLE_" + role.toUpperCase()
                                )
                        )
                        .toList();

        return new JwtAuthenticationToken(
                jwt,
                authorities,
                email
        );
    }
}