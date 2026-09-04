package com.example.demo.service;

import com.example.demo.dto.PlanRequest;
import com.example.demo.dto.PlanResponse;
import com.example.demo.entity.Plan;
import com.example.demo.exception.PlanNameAlreadyExistsException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.PlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Transactional
    public PlanResponse createPlan(PlanRequest request) {

        String planName = request.getName().trim();

        if (planRepository.existsByNameIgnoreCase(planName)) {
            throw new PlanNameAlreadyExistsException(
                    "A plan with this name already exists."
            );
        }

        Plan plan = new Plan();

        plan.setName(planName);
        plan.setPriceCents(request.getPriceCents());
        plan.setBillingCycle(
                request.getBillingCycle().toUpperCase()
        );

        Plan savedPlan = planRepository.save(plan);

        return toResponse(savedPlan);
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> getAllPlans() {

        return planRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlanResponse getPlanById(Long id) {

        Plan plan = planRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Plan not found with id: " + id
                        )
                );

        return toResponse(plan);
    }

    @Transactional
    public PlanResponse updatePlan(
            Long id,
            PlanRequest request) {

        Plan plan = planRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Plan not found with id: " + id
                        )
                );

        String planName = request.getName().trim();

        if (planRepository.existsByNameIgnoreCaseAndIdNot(
                planName,
                id
        )) {
            throw new PlanNameAlreadyExistsException(
                    "A plan with this name already exists."
            );
        }

        plan.setName(planName);
        plan.setPriceCents(request.getPriceCents());
        plan.setBillingCycle(
                request.getBillingCycle().toUpperCase()
        );

        return toResponse(planRepository.save(plan));
    }


    @Transactional
    public void deletePlan(Long id) {

        Plan plan = planRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Plan not found with id: " + id
                        )
                );

        planRepository.delete(plan);
    }

    private PlanResponse toResponse(Plan plan) {

        PlanResponse response = new PlanResponse();

        response.setId(plan.getId());
        response.setName(plan.getName());
        response.setPriceCents(plan.getPriceCents());
        response.setBillingCycle(plan.getBillingCycle());

        return response;
    }
}
