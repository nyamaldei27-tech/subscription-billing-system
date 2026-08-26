package com.example.demo.controller;

import com.example.demo.dto.PaymentRequest;
import com.example.demo.dto.SubscriptionRequest;
import com.example.demo.entity.Invoice;
import com.example.demo.entity.PaymentAttempt;
import com.example.demo.entity.Subscription;
import com.example.demo.service.BillingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/subscriptions")
    public ResponseEntity<Subscription> createSubscription(
            @Valid @RequestBody SubscriptionRequest request) {

        Subscription subscription = billingService.createSubscription(
                request.getCustomerId(),
                request.getPlanId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(subscription);
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<List<Subscription>> getAllSubscriptions() {
        return ResponseEntity.ok(billingService.getAllSubscriptions());
    }

    @GetMapping("/subscriptions/{id}")
    public ResponseEntity<Subscription> getSubscriptionById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                billingService.getSubscriptionById(id)
        );
    }

    @PostMapping("/invoices/{invoiceId}/payment")
    public ResponseEntity<Invoice> payInvoice(
            @PathVariable Long invoiceId,
            @Valid @RequestBody PaymentRequest request) {

        Invoice invoice = billingService.processPayment(
                invoiceId,
                request.getStatus()
        );

        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        return ResponseEntity.ok(
                billingService.getAllInvoices()
        );
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<Invoice> getInvoiceById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                billingService.getInvoiceById(id)
        );
    }

    @GetMapping("/invoices/customer/{customerId}")
    public ResponseEntity<List<Invoice>> getInvoicesByCustomer(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                billingService.getInvoicesByCustomerId(customerId)
        );
    }

    @GetMapping("/payment-attempts")
    public ResponseEntity<List<PaymentAttempt>> getAllPaymentAttempts() {
        return ResponseEntity.ok(
                billingService.getAllPaymentAttempts()
        );
    }

    @GetMapping("/customers/{customerId}/payment-attempts")
    public ResponseEntity<List<PaymentAttempt>> getPaymentAttemptsByCustomerId(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                billingService.getPaymentAttemptsByCustomerId(customerId)
        );
    }
}