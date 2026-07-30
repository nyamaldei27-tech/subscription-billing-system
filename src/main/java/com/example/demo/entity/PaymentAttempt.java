package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_attempts")
public class PaymentAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Invoice association is required")
    @ManyToOne
    @JoinColumn(name="invoice_id",nullable = false)
    private Invoice invoice;

    @NotBlank(message = "Payment status is required")
    @Column(nullable = false)
    private String status;

    @NotNull(message = "Attempt timestamp is required")
    @Column(name = "attempted_at", nullable = false)
    private LocalDateTime attemptedAt=LocalDateTime.now();

    //Getters and Setters

    public Long getId()
    {return id;}
    public void setId(Long id)
    {this.id = id;}
    public Invoice getInvoice()
    {return invoice;}
    public void setInvoice(Invoice invoice)
    {this.invoice = invoice;}
    public String getStatus()
    {return status;}
    public void setStatus(String status)
    {this.status = status;}
    public LocalDateTime getAttemptedAt()
    {return attemptedAt;}
    public void setAttemptedAt(LocalDateTime attemptedAt) {
        this.attemptedAt = attemptedAt;
    }
}
