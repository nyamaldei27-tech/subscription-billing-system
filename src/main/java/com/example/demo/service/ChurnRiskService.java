package com.example.demo.service;

import com.example.demo.client.AccountServiceClient;
import com.example.demo.dto.ChurnRiskResponse;
import com.example.demo.dto.CustomerResponse;
import com.example.demo.entity.SubscriptionActivity;
import com.example.demo.repository.SubscriptionActivityRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChurnRiskService {

    private final SubscriptionActivityRepository subscriptionActivityRepository;
    private final AccountServiceClient accountServiceClient;

    public ChurnRiskService(
            SubscriptionActivityRepository subscriptionActivityRepository,
            AccountServiceClient accountServiceClient) {

        this.subscriptionActivityRepository =
                subscriptionActivityRepository;

        this.accountServiceClient =
                accountServiceClient;
    }

    public ChurnRiskResponse calculateRisk(
            Long customerId,
            Authentication authentication) {

        /*
         * ADMIN users can calculate churn risk
         * for any customer.
         *
         * Normal customers can only calculate
         * their own churn risk.
         */
        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(
                                authority ->
                                        "ROLE_ADMIN"
                                                .equalsIgnoreCase(authority)
                        );

        if (!isAdmin) {

            CustomerResponse currentCustomer =
                    accountServiceClient.getCurrentCustomer(
                            authentication
                    );

            if (!currentCustomer.getId().equals(customerId)) {
                throw new AccessDeniedException(
                        "You are not allowed to access this customer's churn risk."
                );
            }
        }

        List<SubscriptionActivity> activities =
                subscriptionActivityRepository
                        .findByCustomerIdOrderByEventTimestampDesc(
                                customerId
                        );

        LocalDateTime thirtyDaysAgo =
                LocalDateTime.now().minusDays(30);

        int riskScore = 0;

        for (SubscriptionActivity activity : activities) {

            if (activity.getEventTimestamp()
                    .isBefore(thirtyDaysAgo)) {
                continue;
            }

            switch (activity.getEventType()) {

                case "PAYMENT_FAILED" ->
                        riskScore += 30;

                case "SUBSCRIPTION_PLAN_CHANGED" ->
                        riskScore += 10;

                case "PAYMENT_SUCCEEDED" ->
                        riskScore -= 5;

                case "SUBSCRIPTION_CANCELED" ->
                        riskScore += 40;
            }
        }

        riskScore =
                Math.clamp(
                        riskScore,
                        0,
                        100
                );

        String riskLevel;

        if (riskScore >= 60) {
            riskLevel = "HIGH";
        } else if (riskScore >= 30) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "LOW";
        }

        return new ChurnRiskResponse(
                customerId,
                riskScore,
                riskLevel
        );
    }
}