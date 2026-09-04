package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.BillingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    // =========================================================
    // SUBSCRIPTIONS
    // =========================================================

    @PostMapping("/subscriptions")
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @Valid @RequestBody SubscriptionRequest request) {

        SubscriptionResponse response =
                billingService.createSubscription(
                        request.getCustomerId(),
                        request.getPlanId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<List<SubscriptionResponse>>
    getAllSubscriptions() {

        return ResponseEntity.ok(
                billingService.getAllSubscriptions()
        );
    }

    @GetMapping("/subscriptions/{id}")
    public ResponseEntity<SubscriptionResponse>
    getSubscriptionById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                billingService.getSubscriptionById(id)
        );
    }

    @GetMapping("/subscriptions/customer/{customerId}")
    public ResponseEntity<List<SubscriptionResponse>>
    getSubscriptionsByCustomer(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                billingService.getSubscriptionsByCustomerId(
                        customerId
                )
        );
    }

    @PutMapping("/subscriptions/{id}/cancel")
    public ResponseEntity<SubscriptionResponse>
    cancelSubscription(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                billingService.cancelSubscription(id)
        );
    }

    // =========================================================
    // INVOICES
    // =========================================================

    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceResponse>>
    getAllInvoices() {

        return ResponseEntity.ok(
                billingService.getAllInvoices()
        );
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<InvoiceResponse>
    getInvoiceById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                billingService.getInvoiceById(id)
        );
    }

    @GetMapping("/invoices/customer/{customerId}")
    public ResponseEntity<List<InvoiceResponse>>
    getInvoicesByCustomer(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                billingService.getInvoicesByCustomerId(
                        customerId
                )
        );
    }

    // =========================================================
    // PAYMENTS
    // =========================================================

    @PostMapping("/invoices/{invoiceId}/payment")
    public ResponseEntity<InvoiceResponse> payInvoice(
            @PathVariable Long invoiceId,
            @Valid @RequestBody PaymentRequest request) {

        InvoiceResponse invoice =
                billingService.processPayment(
                        invoiceId,
                        request.getStatus()
                );

        return ResponseEntity.ok(invoice);
    }

    // =========================================================
    // PAYMENT ATTEMPTS
    // =========================================================

    @GetMapping("/payment-attempts")
    public ResponseEntity<List<PaymentAttemptResponse>>
    getAllPaymentAttempts() {

        return ResponseEntity.ok(
                billingService.getAllPaymentAttempts()
        );
    }

    @GetMapping(
            "/customers/{customerId}/payment-attempts"
    )
    public ResponseEntity<List<PaymentAttemptResponse>>
    getPaymentAttemptsByCustomerId(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                billingService
                        .getPaymentAttemptsByCustomerId(
                                customerId
                        )
        );
    }
}