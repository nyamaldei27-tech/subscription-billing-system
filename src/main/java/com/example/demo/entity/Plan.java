package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "plans")
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
   private String name;

    @Column(name = "price_Cents", nullable = false)
    private  Integer priceCents;

    @Column(name = "billing_cycle", nullable = false)
    private String billingCycle;

    //Getters and setters
    public Long getId()
    {return id;}
    public void setId(Long id)
    {this.id = id;}

    public String getName()
    {return name;}
    public void setName(String name)
    {this.name = name;}

    public Integer getPriceCents()
    {return priceCents;}
    public void setPriceCents(Integer priceCents)
    {this.priceCents = priceCents;}

    public String getBillingCycle()
    {return billingCycle;}
    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }
}
