package com.example.demo.controller;

import com.example.demo.dto.PlanRequest;
import com.example.demo.dto.PlanResponse;
import com.example.demo.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    public ResponseEntity<PlanResponse> createPlan(
            @Valid @RequestBody PlanRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(planService.createPlan(request));
    }

    @GetMapping
    public ResponseEntity<List<PlanResponse>> getAllPlans() {

        return ResponseEntity.ok(
                planService.getAllPlans()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanResponse> getPlanById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                planService.getPlanById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanResponse> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody PlanRequest request) {

        return ResponseEntity.ok(
                planService.updatePlan(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(
            @PathVariable Long id) {

        planService.deletePlan(id);

        return ResponseEntity.noContent().build();
    }
}