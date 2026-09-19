package com.example.demo.dto;

public class ChurnRiskResponse {

    private final Long customerId;
    private final int riskScore;
    private final String riskLevel;

    public ChurnRiskResponse(
            Long customerId,
            int riskScore,
            String riskLevel) {

        this.customerId = customerId;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }
}
