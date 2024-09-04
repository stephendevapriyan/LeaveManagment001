package com.example.LeaveManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDTO {

    private UUID id;
    private String firstname;
    private String lastname;
    private String email;
    private String role;
    private String phoneNumber;
    private Date hireDate;
    private String jobTitle;
    private UUID organizationId;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
