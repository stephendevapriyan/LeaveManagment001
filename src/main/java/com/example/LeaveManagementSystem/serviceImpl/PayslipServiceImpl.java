package com.example.LeaveManagementSystem.serviceImpl;

import com.example.LeaveManagementSystem.dto.CreatePayslipDTO;
import com.example.LeaveManagementSystem.dto.CreatePayslipPDFDTO;
import com.example.LeaveManagementSystem.entity.EmployeeEntity;
import com.example.LeaveManagementSystem.entity.PayslipEntity;

import com.example.LeaveManagementSystem.entity.PayslipPdf;
import com.example.LeaveManagementSystem.repository.EmployeeRepo;
import com.example.LeaveManagementSystem.repository.PdfPayslip;
import com.example.LeaveManagementSystem.service.LeaveService;
import com.example.LeaveManagementSystem.service.PayslipService;


import com.example.LeaveManagementSystem.repository.PayslipRep;
import com.example.LeaveManagementSystem.utils.ErrorUtil;

import lombok.extern.slf4j.Slf4j;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PayslipServiceImpl implements PayslipService {
    private final PayslipRep payslipRepository;
    private final LeaveService leaveService;
    EmployeeRepo employeeRepo;
    PdfPayslip pdfsave;

    public PayslipServiceImpl(PayslipRep payslipRepository, LeaveService leaveService, EmployeeRepo employeeRepo, PdfPayslip pdfsave) {
        this.payslipRepository = payslipRepository;
        this.leaveService = leaveService;
        this.pdfsave = pdfsave;
        this.employeeRepo = employeeRepo;

    }

    public ErrorUtil<String, String> save(CreatePayslipDTO dto) {
        var employee = this.leaveService.isEmployeeExists(dto.employeeId());
        if (!employee) {
            return new ErrorUtil<>(false, "employee with UUID is not found", null);
        }
        byte[] filePart = dto.file(); // You need to have a method to retrieve the file part

        var emp = EmployeeEntity.builder()
                .id(dto.employeeId()).build();

        var payslip = PayslipEntity.builder()
                .file(filePart)
                .fileName(dto.fileName())
                .fileType(dto.fileType())
                .employeeEntity(emp)
                .issuedDate(dto.issuedDate())
                .payPeriodStart(dto.payPeriodStart())
                .payPeriodEnd(dto.payPeriodEnd())
                .build();

        this.payslipRepository.save(payslip);

        return new ErrorUtil<>(true, null, "payslip information saved successfully");
    }

    public ErrorUtil<String, ByteArrayOutputStream> generatePDF(CreatePayslipPDFDTO dto) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            // Check if the UUID is valid
            UUID employeeId;
            try {
                employeeId = UUID.fromString(String.valueOf(dto.getId()));
            } catch (IllegalArgumentException e) {
                log.error("Invalid UUID format: " + dto.getId(), e);
                return new ErrorUtil<>(false, "Invalid UUID format: " + dto.getId(), null);
            }

            // Create a PdfWriter instance
            PdfWriter writer = new PdfWriter(stream);

            // Create a PdfDocument instance
            PdfDocument pdf = new PdfDocument(writer);

            // Create a Document instance
            Document document = new Document(pdf);

            try {
                // Add content to the PDF
                document.add(new Paragraph("Payslip"));
                document.add(new Paragraph("ID: " + dto.getId()));
                document.add(new Paragraph("Employee Name: " + dto.getEmployeeName()));
                document.add(new Paragraph("Employee ID: " + dto.getEmployeeId()));
                document.add(new Paragraph("Designation: " + dto.getDesignation()));
                document.add(new Paragraph("Date of Joining: " + dto.getDateOfJoining()));
                document.add(new Paragraph("PAN: " + dto.getPan()));
                document.add(new Paragraph("PF No: " + dto.getPfNo()));
                document.add(new Paragraph("Insurance No: " + dto.getInsuranceNo()));
                document.add(new Paragraph("Pay Month: " + dto.getPayMonth()));
                document.add(new Paragraph("Pay Period: " + dto.getPayPeriodBeginDate() + " to " + dto.getPayPeriodEndDate()));
                document.add(new Paragraph("Total Month Days: " + dto.getTotalMonthDays()));
                document.add(new Paragraph("Number of Present Days: " + dto.getNoOfPresentDays()));
                document.add(new Paragraph("Number of Loss of Pay: " + dto.getNoOfLossOfPay()));
                document.add(new Paragraph("CTC Per Month: " + dto.getCtcPerMonth()));
                document.add(new Paragraph("Basic: " + dto.getBasic()));
                document.add(new Paragraph("City Compensation Allowance: " + dto.getCityCompensationAllowance()));
                document.add(new Paragraph("Conveyance Allowance: " + dto.getConveyanceAllowance()));
                document.add(new Paragraph("HRA: " + dto.getHra()));
                document.add(new Paragraph("Salary Arrears: " + dto.getSalaryArears()));
                document.add(new Paragraph("Variable Allowances: " + dto.getVariableAllowances()));
                document.add(new Paragraph("Gross Salary: " + dto.getGrossSalary()));
                document.add(new Paragraph("PF: " + dto.getPf()));
                document.add(new Paragraph("Professional Tax: " + dto.getProfessionalTax()));
                document.add(new Paragraph("TDS: " + dto.getTds()));
                document.add(new Paragraph("Insurance: " + dto.getInsurance()));
                document.add(new Paragraph("Loan and Advances: " + dto.getLoanAndAdvances()));
                document.add(new Paragraph("Gross Deductions: " + dto.getGrossDeductions()));
                document.add(new Paragraph("Total Earnings: " + dto.getTotalEarnings()));
                document.add(new Paragraph("Total Deductions: " + dto.getTotalDeductions()));
                document.add(new Paragraph("Net Pay: " + dto.getNetPay()));
            } catch (Exception e) {
                log.error("Error adding content to PDF: " + e.getMessage(), e);
                return new ErrorUtil<>(false, "Error adding content to PDF: " + e.getMessage(), null);
            } finally {
                // Ensure document is closed to release resources
                document.close();
            }

            // Save the PDF to the database
            try {
                PayslipPdf pdfs = new PayslipPdf();
                Optional<EmployeeEntity> byId = employeeRepo.findById(employeeId);
                if (byId.isPresent()) {
                    pdfs.setId(employeeId);
                    pdfs.setFile(stream.toByteArray());
                    pdfsave.save(pdfs);
                } else {
                    log.warn("Invalid employee ID: " + dto.getId());
                    return new ErrorUtil<>(false, "Invalid employee ID: " + dto.getId(), null);
                }
            } catch (Exception e) {
                log.error("Error saving PDF to database: " + e.getMessage(), e);
                return new ErrorUtil<>(false, "Error saving PDF to database: " + e.getMessage(), null);
            }

        } catch (Exception e) {
            log.error("Error generating PDF: " + e.getMessage(), e);
            return new ErrorUtil<>(false, "Error generating PDF: " + e.getMessage(), null);
        } finally {
            // Cleanup resources if necessary
            try {
                stream.close();
            } catch (IOException e) {
                log.error("Error closing ByteArrayOutputStream: " + e.getMessage(), e);
            }
        }

        return new ErrorUtil<>(true, null, stream);
    }
    public List<byte[]> getAllPdfFiles() {
        try {
            // Retrieve all PayslipPdf entities from the database
            List<PayslipPdf> pdfList = pdfsave.findAll();

            if (pdfList.isEmpty()) {

                return Collections.emptyList(); // Return an empty list if no PDFs are found
            }

            // Map the list of PayslipPdf entities to a list of byte arrays
            List<byte[]> pdfFiles = pdfList.stream()
                    .map(PayslipPdf::getFile) // Extract the byte[] from each PayslipPdf
                    .collect(Collectors.toList()); // Collect into a List of byte[]

            return pdfFiles;
        } catch (Exception e) {
            // Log the exception

            log.error("Error occurred while retrieving PDF files", e);
            throw new RuntimeException("An error occurred while retrieving PDF files", e);
        }
    }}