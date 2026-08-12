package com.example.demo.dto;


import jakarta.validation.constraints.NotNull;

public class SubscriptionRequest {
    @NotNull(message = "Customer ID cannot e null")
    private Long CustomerId;
    @NotNull(message = "Plan ID cannot be null")
    private Long PlanId;


    public Long getCustomerId() { return CustomerId; }
    public void setCustomerId(Long CustomerId) { this.CustomerId = CustomerId; }
    public Long getPlanId() { return PlanId; }
    public void setPlanId(Long PlanId) { this.PlanId = PlanId; }
}
