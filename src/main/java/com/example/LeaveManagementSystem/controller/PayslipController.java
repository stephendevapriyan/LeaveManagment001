package com.example.LeaveManagementSystem.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.LeaveManagementSystem.dto.CreatePayslipDTO;
import com.example.LeaveManagementSystem.dto.CreatePayslipPDFDTO;
import com.example.LeaveManagementSystem.response.ApiResponse;
import com.example.LeaveManagementSystem.service.PayslipService;
import com.example.LeaveManagementSystem.utils.Utils;

@RestController
@RequestMapping("/api/payslip")
public class PayslipController {

    private final PayslipService payslipService;
    private final Utils utils;

    public PayslipController(PayslipService payslipService, Utils utils) {
        this.payslipService = payslipService;
        this.utils = utils;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> createPaySlip(
            @RequestPart("file") MultipartFile file,
            @RequestPart("name") String name,
            @RequestPart("pay_period_start") String payPeriodStart,
            @RequestPart("pay_period_end") String payPeriodEnd,
            @RequestPart("issued_date") String issuedDate,
            @RequestPart("file_type") String fileType,
            @RequestPart("file_name") String fileName,
            @RequestPart("employee_id") String employeeId) throws Exception {

        try {
            LocalDate payPeriodStartDate = utils.stringToLocalDate(payPeriodStart);
            LocalDate payPeriodEndDate = utils.stringToLocalDate(payPeriodEnd);
            LocalDate issDate = utils.stringToLocalDate(issuedDate);

            var dto = new CreatePayslipDTO(
                    UUID.fromString(employeeId),
                    payPeriodStartDate,
                    payPeriodEndDate,
                    issDate,
                    file.getBytes(),
                    fileType,
                    fileName);

            var result = payslipService.save(dto);
            if (!result.isOk()) {
                return new ApiResponse<String>("Invalid input", null, result.getMessage());
            }

            return new ApiResponse<Object>("Everything looks good", 200, null);
        } catch (DateTimeParseException e) {
            return new ApiResponse<String>("Invalid date time format", 0, e.getMessage());
        }
    }

    @PostMapping("/create-pdf")
    public byte[] genereatePDF(@RequestBody CreatePayslipPDFDTO dto) {
        var res = payslipService.generatePDF(dto);
        if (!res.isOk()) {
            return null;
        }
        ContentDisposition contentDisposition = ContentDisposition.builder("inline")
                .filename(dto.getName() + "-paylip" + "-" + dto.getPayMonth().toString())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);
        return res.getData().toByteArray();
    }
}
