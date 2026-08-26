package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Customer belongs outside the billing domain,
     * so we store the customer's ID rather than a JPA relationship.
     */
    @NotNull(message = "Customer association is required")
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @NotNull(message = "Plan association is required")
    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @NotBlank(message = "Subscription status is required")
    @Pattern(regexp = "^(ACTIVE|PAST_DUE)$", message = "Subscription status must be ACTIVE or PAST_DUE")
    @Column(nullable = false)
    private String status;

    @NotNull(message = "Current period end date is required")
    @Column(name = "current_period_end", nullable = false)
    private LocalDateTime currentPeriodEnd;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCurrentPeriodEnd() {
        return currentPeriodEnd;
    }

    public void setCurrentPeriodEnd(LocalDateTime currentPeriodEnd) {
        this.currentPeriodEnd = currentPeriodEnd;
    }
}
