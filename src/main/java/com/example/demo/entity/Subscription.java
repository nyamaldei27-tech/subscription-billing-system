package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Customer association is required")
    @ManyToOne
    @JoinColumn(name = "customer_id",nullable = false)
    private Customer customer;

    @NotNull(message = "Plan association is required")
    @ManyToOne
    @JoinColumn(name = "plan_id",nullable = false)
    private Plan plan;

    @NotBlank(message = "Subscription status is required")
    @Column(nullable = false)
    private String status; //e.g active,past_due

    @NotNull(message = "Current period end date is required")
    @Column(name = "current_period_end", nullable = false)
    private LocalDateTime currentPeriodEnd;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate = LocalDateTime.now();

    @Column(name = "next_billing_date")
    private LocalDateTime nextBillingDate;

    //Getters and setters
    public Long getId()
    {return id;}
    public void setId(Long id)
    {this.id = id;}
    public Customer getCustomer()
    {return customer;}
    public void setCustomer(Customer customer)
    {this.customer = customer;}
    public Plan getPlan()
    {return plan;}
    public void setPlan(Plan plan)
    {this.plan = plan;}
    public String getStatus()
    {return status;}
    public void setStatus(String status)
    {this.status = status;}
    public LocalDateTime getCurrentPeriodEnd()
    {return currentPeriodEnd;}
    public void setCurrentPeriodEnd(LocalDateTime currentPeriodEnd)
    {this.currentPeriodEnd = currentPeriodEnd;}

    }
