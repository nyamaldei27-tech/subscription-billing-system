package com.example.demo.repository;

import com.example.demo.entity.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {
    // Spring Data JPA automatically generates the SQL join across Invoice and Subscription to find the Customer ID!
    List<PaymentAttempt> findByInvoiceSubscriptionCustomerId(Long customerId);
}
