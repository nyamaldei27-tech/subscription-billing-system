package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public class PlanRequest {

    @NotBlank(message = "Plan name is required")
    private String name;

    @NotBlank(message = "Billing cycle is required")
    @Pattern(
            regexp = "^(WEEKLY|MONTHLY|YEARLY)$",
            message = "Billing cycle must be WEEKLY, MONTHLY, or YEARLY"
    )
    private String billingCycle;

    @NotNull(message = "Price in cents is required")
    @PositiveOrZero(message = "Price cents must be zero or a positive value")
    private Long priceCents;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public Long getPriceCents() {
        return priceCents;
    }

    public void setPriceCents(Long priceCents) {
        this.priceCents = priceCents;
    }
}
