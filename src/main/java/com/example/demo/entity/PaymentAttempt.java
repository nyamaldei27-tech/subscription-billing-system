package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_attempts")
public class PaymentAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="invoice_id",nullable = false)
    private Invoice invoice;

    @Column(nullable = false)
    private String status;

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
