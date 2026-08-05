package com.example.demo.repository;

import com.example.demo.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    //Find subscriptions that are ACTIVE and whose billing period has expired/is due
    List<Subscription> findByStatusAndCurrentPeriodEndLessThanEqual(String Status,LocalDateTime Date);

}
