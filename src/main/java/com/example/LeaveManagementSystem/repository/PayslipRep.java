package com.example.LeaveManagementSystem.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.LeaveManagementSystem.entity.PayslipEntity;

@Repository
public interface PayslipRep extends JpaRepository<PayslipEntity, UUID> {
}