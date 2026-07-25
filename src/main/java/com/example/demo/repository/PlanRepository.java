package com.example.demo.repository;

import com.example.demo.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanRepository extends JpaRepository<Plan,Long> {
    boolean existsByName(String name);
}
