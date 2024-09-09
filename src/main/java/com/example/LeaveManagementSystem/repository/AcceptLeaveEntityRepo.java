package com.example.LeaveManagementSystem.repository;

import java.util.Optional;
import java.util.UUID;

import com.example.LeaveManagementSystem.entity.AcceptLeave;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AcceptLeaveEntityRepo extends JpaRepository<AcceptLeave, UUID> {

    Optional<AcceptLeave> findByLeaveRequestId(UUID leaveRequestId);



}
