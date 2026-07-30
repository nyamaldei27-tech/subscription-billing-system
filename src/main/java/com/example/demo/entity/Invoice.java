package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Entity
@Table(name = "Invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Subscription relation is required")
    @ManyToOne
    @JoinColumn(name ="subscription_id", nullable = false)
    private Subscription subscription;

    @NotNull(message = "Amount cents is required")
    @PositiveOrZero(message = "Amount cents must be zero or a positive value")
    @Column(name = "amount_cents", nullable = false)
    private Integer amountCents;

    @NotBlank(message = "Invoice status is required")
    @Column(nullable = false)
    private String status; //e.g paid, failed

    @NotNull(message = "Due date is required")
    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    //Getters and setters

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public Subscription getSubscription() {return subscription;}
    public void setSubscription(Subscription subscription) {this.subscription = subscription;}
    public Integer getAmountCents() {return amountCents;}
    public void setAmountCents(Integer amountCents) {this.amountCents = amountCents;}
    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}
    public LocalDateTime getDueDate() {return dueDate;}
    public void setDueDate(LocalDateTime dueDate) {this.dueDate = dueDate;}


}
