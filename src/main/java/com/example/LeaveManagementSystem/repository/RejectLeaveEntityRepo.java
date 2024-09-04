package com.example.LeaveManagementSystem.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.LeaveManagementSystem.entity.RejectLeaveEntity;

public interface RejectLeaveEntityRepo extends JpaRepository<RejectLeaveEntity, UUID> {
}
