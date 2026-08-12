package com.example.demo.controller;

import com.example.demo.dto.PaymentRequest;
import com.example.demo.dto.SubscriptionRequest;
import com.example.demo.entity.Invoice;
import com.example.demo.entity.PaymentAttempt;
import com.example.demo.entity.Subscription;
import com.example.demo.service.BillingService;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;// Covers RestController,Mapping,RequestBody,PathVariable,CrossOrigin
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*") //<--allows the web code to access these endpoints
@RequestMapping("/api")
@Validated
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    /**
     * Endpoint to purchase a subscription
     * Expects JSON: { "customerId": 1, "planId": 2 }
     */
    @PostMapping("/subscriptions")
    public ResponseEntity<?> subscribe(@RequestBody SubscriptionRequest payload) {

        // Keep a simple fallback check in case the keys themselves are entirely missing from the JSON
        if (payload.getCustomerId() == null || payload.getPlanId() == null) {
            return ResponseEntity.badRequest().body("Both customerId and planId must be provided in the request body.");
        }

        try {
            Subscription subscription = billingService.createSubscription(payload.getCustomerId(), payload.getPlanId());
            return ResponseEntity.ok(subscription);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<List<Subscription>> getAllSubscriptions() {
        return ResponseEntity.ok(billingService.getAllSubscriptions());
    }

    @GetMapping("/subscriptions/{id}")
    public ResponseEntity<Subscription> getSubscriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getSubscriptionById(id));
    }


    /**
     * Endpoint to simulate paying an invoice
     * Expects JSON: { "status": "SUCCESS" } or { "status": "FAILED" }
     */
    @PostMapping("/invoices/{invoiceId}/payment")
    public ResponseEntity<?> payInvoice(@PathVariable Long invoiceId, @RequestBody Map<String, @NotBlank @Pattern(regexp = "^(?i)(SUCCESS|FAILED)$", message = "Must be SUCCESS or FAILED") String> payload) {
        String status = payload.get("status");

        if (status == null || (!status.equalsIgnoreCase("SUCCESS") && !status.equalsIgnoreCase("FAILED"))) {
            return ResponseEntity.badRequest().body("Invalid payment status. Must be SUCCESS or FAILED");
        }

        try {
            Invoice invoice = billingService.processPayment(invoiceId, status);
            return ResponseEntity.ok(invoice);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/invoices/customer/{customerId}")
    public ResponseEntity<?> getInvoicesByCustomer(@PathVariable Long customerId) {
        try {
            List<Invoice> invoices = billingService.getInvoicesByCustomerId(customerId);
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/invoices")
    public ResponseEntity<?> getAllInvoices() {
        try {
            List<Invoice> invoices = billingService.getAllInvoices();
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getInvoiceById(id));
    }

    @PostMapping("/payments/process")
    public ResponseEntity<Invoice> processPayment(@RequestBody @Valid PaymentRequest payload) {
       try {
           Invoice invoice = billingService.processPayment(payload.getInvoiceId(), payload.getStatus());
           return ResponseEntity.ok(invoice);

       } catch (IllegalArgumentException e) {
           return ResponseEntity.badRequest().body(null);
       }
    }

    @GetMapping("/payment-attempts")
    public ResponseEntity<List<PaymentAttempt>> getAllPaymentAttempts() {
        return ResponseEntity.ok(billingService.getAllPaymentAttempts());
    }

    @GetMapping("/customers/{customerId}/payment-attempts")
    public ResponseEntity<List<PaymentAttempt>> getPaymentAttemptsByCustomerId(@PathVariable Long customerId) {
        List<PaymentAttempt> attempts = billingService.getPaymentAttemptsByCustomerId(customerId);
        return ResponseEntity.ok(attempts);
    }
}