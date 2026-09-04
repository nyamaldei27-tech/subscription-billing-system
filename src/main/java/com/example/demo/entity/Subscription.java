package com.example.demo.entity;

import jakarta.persistence.*;
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
     * Customer belongs to the account service.
     * Therefore, we store only the customer ID.
     */
    @NotNull(message = "Customer association is required")
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /*
     * Plan belongs to the billing domain,
     * so a JPA relationship is appropriate here.
     */
    @NotNull(message = "Plan association is required")
    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @NotNull(message = "Subscription status is required")
    @Pattern(
            regexp = "^(ACTIVE|PAST_DUE|CANCELED)$",
            message = "Subscription status must be ACTIVE, PAST_DUE or CANCELED"
    )
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