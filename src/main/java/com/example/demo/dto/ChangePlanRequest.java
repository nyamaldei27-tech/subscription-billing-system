package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;

public class ChangePlanRequest {
    @NotNull(message = "Plan ID cannot be null")
    private Long planId;

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }
}
