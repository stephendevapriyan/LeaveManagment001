package com.example.LeaveManagementSystem.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class CreatePayslipPDFDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;
    private String employeeName;
    private String employeeId;
    private String designation;
    private LocalDate dateOfJoining;
    private String pan;
    private String pfNo;
    private String insuranceNo;

    private String payMonth;
    private LocalDate payPeriodBeginDate;
    private LocalDate payPeriodEndDate;
    private int totalMonthDays;
    private int noOfPresentDays;
    private int noOfLossOfPay;
    private double ctcPerMonth;

    private double basic;
    private double cityCompensationAllowance;
    private double conveyanceAllowance;
    private double hra;
    private double salaryArears;
    private double variableAllowances;
    private double grossSalary;

    private double pf;
    private double professionalTax;
    private double tds;
    private double insurance;
    private double loanAndAdvances;
    private double grossDeductions;

    private double totalEarnings;
    private double totalDeductions;
    private double netPay;

}
