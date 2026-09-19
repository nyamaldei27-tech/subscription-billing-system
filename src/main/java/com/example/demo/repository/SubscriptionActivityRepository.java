package com.example.demo.repository;

import com.example.demo.entity.SubscriptionActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionActivityRepository
        extends JpaRepository<SubscriptionActivity, Long> {

    List<SubscriptionActivity> findByCustomerIdOrderByEventTimestampDesc(Long customerId);
}