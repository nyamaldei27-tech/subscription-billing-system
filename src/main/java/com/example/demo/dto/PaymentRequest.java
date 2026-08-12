package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;

public class PaymentRequest {
    @NotNull
    private Long invoiceId;
    @NotNull
    private String status;

    public Long getInvoiceId() {
            return invoiceId;
    }
    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }
    public String getStatus() {
        return status;
    }
}
