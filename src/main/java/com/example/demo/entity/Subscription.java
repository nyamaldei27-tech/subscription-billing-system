package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id",nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "plan_id",nullable = false)
    private Plan plan;

    @Column(nullable = false)
    private String status; //e.g active,past_due

    @Column(name = "current_period_end", nullable = false)
    private LocalDateTime currentPeriodEnd;

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
