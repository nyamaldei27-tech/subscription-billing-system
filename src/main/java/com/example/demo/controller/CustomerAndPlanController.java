package com.example.demo.controller;

import com.example.demo.entity.Customer;
import com.example.demo.entity.Plan;
import com.example.demo.service.BillingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class CustomerAndPlanController {


    private final BillingService billingService;

    public CustomerAndPlanController(BillingService billingService) {
        this.billingService = billingService;
    }

    // --- Customer Routes ---

    @PostMapping("/customers")
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        Customer savedCustomer = billingService.createCustomer(customer);
        return ResponseEntity.ok(savedCustomer);
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getCustomerById(id));
    }

    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(billingService.getAllCustomers());
    }

    // --- Plan Routes ---

    @PostMapping("/plans")
    public ResponseEntity<Plan> createPlan(@Valid @RequestBody Plan plan) {
        Plan savedPlan = billingService.createPlan(plan);
        return ResponseEntity.ok(savedPlan);
    }

    @GetMapping("/plans/{id}")
    public ResponseEntity<Plan> getPlanById(@PathVariable Long id) {
        Plan plan = billingService.getPlanById(id);
        return ResponseEntity.ok(plan);
    }

    @GetMapping("/plans")
    public ResponseEntity< List<Plan>> getAllPlans() {
        List<Plan> Plans = billingService.getAllPlans();

        return ResponseEntity.ok(Plans);
    }
}