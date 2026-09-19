package com.example.demo.repository;

import com.example.demo.entity.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentAttemptRepository
        extends JpaRepository<PaymentAttempt, Long> {

    @Query("""
            SELECT p
            FROM PaymentAttempt p
            JOIN p.invoice i
            JOIN i.subscription s
            WHERE s.customerId = :customerId
            """)
    List<PaymentAttempt> findByCustomerId(
            @Param("customerId") Long customerId
    );
}
