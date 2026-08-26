package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class PaymentRequest {

    @NotBlank(message = "Payment status is required")
    @Pattern(
            regexp = "^(SUCCESS|FAILED)$",
            message = "Payment status must be SUCCESS or FAILED"
    )
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
