package com.example.demo.controller;

import com.example.demo.entity.Customer;
import com.example.demo.entity.Plan;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.PlanRepository;
import com.example.demo.service.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class CustomerAndPlanController {

    private final CustomerRepository customerRepository;
    private final PlanRepository planRepository;
    private final BillingService billingService;

    public CustomerAndPlanController(CustomerRepository customerRepository, PlanRepository planRepository, BillingService billingService) {
        this.customerRepository = customerRepository;
        this.planRepository = planRepository;
        this.billingService = billingService;
    }

    // --- Customer Routes ---

    @PostMapping("/customers")
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        if (customer.getName() == null || customer.getEmail() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(customerRepository.save(customer));
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getCustomerById(id));
    }

    // --- Plan Routes ---

    @PostMapping("/plans")
    public ResponseEntity<Plan> createPlan(@RequestBody Plan plan) {
        if ((plan.getName()==null || plan.getPriceCents()== null || plan.getBillingCycle()==null)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(planRepository.save(plan));
    }

    @GetMapping("/plans")
    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }
}