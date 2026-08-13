package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;


public class PlanRequest {
    @NotBlank(message = "Plan name is required")
    private String name;
    @NotBlank(message = "Billing cyccle is required")
    private String billingCycle;
    @NotNull(message = "Price in cents is required")
    @PositiveOrZero(message = "Price cents must be zero or positive value")
    private int priceCents;

    public String getName() {
        return name;
    }
    public void setName(String name) {}

    public int getPriceCents() { return priceCents; }
    public void setPriceCents(int priceCents) { this.priceCents = priceCents; }
    public String getBillingCycle() { return billingCycle; }
    public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }

}
