package com.example.demo.controller;

import com.example.demo.entity.Invoice;
import com.example.demo.entity.Subscription;
import com.example.demo.service.BillingService;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;// Covers RestController,Mapping,RequestBody,PathVariable,CrossOrigin

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*") //<--allows the web code to access these endpoints
@RequestMapping("/api/billing")
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
    public ResponseEntity<?> subscribe(@RequestBody Map<String, @NotNull(message = "ID cannot be null") Long> payload) {
        Long customerId = payload.get("customerId");
        Long planId = payload.get("planId");

        // Keep a simple fallback check in case the keys themselves are entirely missing from the JSON
        if (customerId == null || planId == null) {
            return ResponseEntity.badRequest().body("Missing customerId or planId");
        }

        try {
            Subscription subscription = billingService.createSubscription(customerId, planId);
            return ResponseEntity.ok(subscription);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
}