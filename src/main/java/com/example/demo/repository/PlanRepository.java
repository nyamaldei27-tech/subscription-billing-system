package com.example.demo.repository;

import com.example.demo.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanRepository extends JpaRepository<Plan,Long> {

    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsByNameIgnoreCaseAndIdNot(String planName, Long id);
}

