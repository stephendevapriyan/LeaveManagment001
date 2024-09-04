package com.example.LeaveManagementSystem.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.LeaveManagementSystem.entity.AcceptLeaveEntity;

public interface AcceptLeaveEntityRepo extends JpaRepository<AcceptLeaveEntity, UUID> {

}
