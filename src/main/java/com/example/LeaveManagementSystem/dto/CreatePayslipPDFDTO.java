package com.example.LeaveManagementSystem.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class CreatePayslipPDFDTO {
    private String name;
    private Integer id;
    private String designation;

    private LocalDate payMonth;
    private LocalDate beginDate;
    private LocalDate endDate;
    private Integer lossOfPay;
    private Integer presentDays;
}
