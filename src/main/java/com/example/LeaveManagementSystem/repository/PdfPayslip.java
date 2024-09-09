package com.example.LeaveManagementSystem.repository;

import com.example.LeaveManagementSystem.entity.PayslipPdf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PdfPayslip extends JpaRepository<PayslipPdf, UUID> {
}
