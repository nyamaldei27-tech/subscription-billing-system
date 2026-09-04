package com.example.demo.repository;

import com.example.demo.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long> {

    List<Subscription> findByStatusAndCurrentPeriodEndLessThanEqual(
            String status,
            LocalDateTime date
    );

    List<Subscription> findByCustomerId(Long customerId);
}
