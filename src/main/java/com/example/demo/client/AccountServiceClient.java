package com.example.demo.client;

import com.example.demo.dto.CustomerResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AccountServiceClient {

    private final RestClient restClient;

    public AccountServiceClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://account-service:8081")
                .build();
    }

    public boolean customerExists(
            Long customerId,
            Authentication authentication) {

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
            throw new IllegalStateException(
                    "Authenticated user does not have a JWT"
            );
        }

        String tokenValue =
                jwtAuthenticationToken.getToken().getTokenValue();

        try {
            restClient.get()
                    .uri("/api/accounts/{id}", customerId)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + tokenValue
                    )
                    .retrieve()
                    .toBodilessEntity();

            return true;

        } catch (
                org.springframework.web.client.HttpClientErrorException.NotFound e) {

            return false;
        }
    }

    public CustomerResponse getCurrentCustomer(
            Authentication authentication) {

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
            throw new IllegalStateException(
                    "Authenticated user does not have a JWT"
            );
        }

        String tokenValue =
                jwtAuthenticationToken.getToken().getTokenValue();

        return restClient.get()
                .uri("/api/accounts/me")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokenValue
                )
                .retrieve()
                .body(CustomerResponse.class);
    }
}