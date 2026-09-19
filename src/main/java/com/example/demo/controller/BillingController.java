package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.SubscriptionActivity;
import com.example.demo.service.BillingService;
import com.example.demo.service.ChurnRiskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BillingController {

    private final BillingService billingService;
    private final ChurnRiskService churnRiskService;

    public BillingController(
            BillingService billingService,
            ChurnRiskService churnRiskService) {

        this.billingService = billingService;
        this.churnRiskService = churnRiskService;
    }

    // =========================
    // SUBSCRIPTIONS
    // =========================

    @PostMapping("/subscriptions")
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @Valid @RequestBody SubscriptionRequest request,
            Authentication authentication) {

        SubscriptionResponse response =
                billingService.createSubscription(
                        request.getCustomerId(),
                        request.getPlanId(),
                        authentication
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptions() {
        return ResponseEntity.ok(
                billingService.getAllSubscriptions()
        );
    }

    @GetMapping("/subscriptions/{id}")
    public ResponseEntity<SubscriptionResponse> getSubscriptionById(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                billingService.getSubscriptionById(
                        id,
                        authentication
                )
        );
    }

    @GetMapping("/subscriptions/customer/{customerId}")
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptionsByCustomer(
            @PathVariable Long customerId,
            Authentication authentication) {

        return ResponseEntity.ok(
                billingService.getSubscriptionsByCustomerId(
                        customerId,
                        authentication
                )
        );
    }

    @PutMapping("/subscriptions/{id}/cancel")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                billingService.cancelSubscription(
                        id,
                        authentication
                )
        );
    }

    @PutMapping("/subscriptions/{id}/plan")
    public ResponseEntity<SubscriptionResponse> changeSubscriptionPlan(
            @PathVariable Long id,
            @Valid @RequestBody ChangePlanRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                billingService.changeSubscriptionPlan(
                        id,
                        request.getPlanId(),
                        authentication
                )
        );
    }

    // =========================
    // INVOICES
    // =========================

    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceResponse>> getAllInvoices() {
        return ResponseEntity.ok(
                billingService.getAllInvoices()
        );
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<InvoiceResponse> getInvoiceById(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                billingService.getInvoiceById(
                        id,
                        authentication
                )
        );
    }

    @GetMapping("/invoices/customer/{customerId}")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByCustomer(
            @PathVariable Long customerId,
            Authentication authentication) {

        return ResponseEntity.ok(
                billingService.getInvoicesByCustomerId(
                        customerId,
                        authentication
                )
        );
    }

    // =========================
    // PAYMENTS
    // =========================

    @PostMapping("/invoices/{invoiceId}/payment")
    public ResponseEntity<InvoiceResponse> payInvoice(
            @PathVariable Long invoiceId,
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication) {

        InvoiceResponse invoice =
                billingService.processPayment(
                        invoiceId,
                        request.getStatus(),
                        authentication
                );

        return ResponseEntity.ok(invoice);
    }

    // =========================
    // PAYMENT ATTEMPTS
    // =========================

    @GetMapping("/payment-attempts")
    public ResponseEntity<List<PaymentAttemptResponse>> getAllPaymentAttempts() {
        return ResponseEntity.ok(
                billingService.getAllPaymentAttempts()
        );
    }

    @GetMapping("/payment-attempts/customer/{customerId}")
    public ResponseEntity<List<PaymentAttemptResponse>>
    getPaymentAttemptsByCustomerId(
            @PathVariable Long customerId,
            Authentication authentication) {

        return ResponseEntity.ok(
                billingService.getPaymentAttemptsByCustomerId(
                        customerId,
                        authentication
                )
        );
    }

    // =========================
    // SUBSCRIPTION ACTIVITIES
    // =========================

    @GetMapping("/subscription-activities/customer/{customerId}")
    public List<SubscriptionActivity>
    getSubscriptionActivitiesByCustomerId(
            @PathVariable Long customerId,
            Authentication authentication) {

        return billingService.getSubscriptionActivitiesByCustomerId(
                customerId,
                authentication
        );
    }

    // =========================
    // CHURN RISK
    // =========================

    @GetMapping("/churn-risk/customer/{customerId}")
    public ChurnRiskResponse getChurnRisk(
            @PathVariable Long customerId,
            Authentication authentication) {

        return churnRiskService.calculateRisk(
                customerId,
                authentication
        );
    }
}