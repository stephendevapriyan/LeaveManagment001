package com.example.LeaveManagementSystem.repository;

import com.example.LeaveManagementSystem.entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepo extends JpaRepository<EmployeeEntity, UUID> {

    Optional<EmployeeEntity> findByEmail(String email);

}
