package com.example.LeaveManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveResponseDTO {

    private UUID id;
    private UUID employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String leaveType;
    private String status;
    private LocalDateTime requestDate;
    private String leaveReason;
    private LocalDateTime approvedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
