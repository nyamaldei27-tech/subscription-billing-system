package com.example.demo.client;

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

    public boolean customerExists(Long customerId) {

        try {
            restClient.get()
                    .uri("/api/accounts/{id}", customerId)
                    .retrieve()
                    .toBodilessEntity();

            return true;

        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            return false;
        }
    }
}