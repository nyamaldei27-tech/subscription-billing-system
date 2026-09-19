package com.example.demo.kafka;

import java.time.LocalDateTime;

public class BillingEvent {
    private String eventType;
    private Long subscriptionId;
    private Long customerId;
    private Long planId;
    private LocalDateTime timestamp;

    public BillingEvent() {
    }

    public BillingEvent(
            String eventType,
            Long subscriptionId,
            Long customerId,
            Long planId,
            LocalDateTime timestamp) {

        this.eventType = eventType;
        this.subscriptionId = subscriptionId;
        this.customerId = customerId;
        this.planId = planId;
        this.timestamp = timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
