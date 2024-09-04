package com.example.LeaveManagementSystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.LeaveManagementSystem.entity.WorkingHours;

import java.util.UUID;

public interface WorkingHoursRepo extends JpaRepository<WorkingHours, UUID>  {

    
} 
