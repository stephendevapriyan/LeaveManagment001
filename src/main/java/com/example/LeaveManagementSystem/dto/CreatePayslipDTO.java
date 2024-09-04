package com.example.LeaveManagementSystem.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreatePayslipDTO(
        UUID employeeId,
        LocalDate payPeriodStart,
        LocalDate payPeriodEnd,
        LocalDate issuedDate,
        byte[] file,
        String fileType,
        String fileName) {
}
