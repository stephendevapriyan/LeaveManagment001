package com.example.LeaveManagementSystem.serviceImpl;

import com.example.LeaveManagementSystem.dto.CreatePayslipDTO;
import com.example.LeaveManagementSystem.dto.CreatePayslipPDFDTO;
import com.example.LeaveManagementSystem.entity.EmployeeEntity;
import com.example.LeaveManagementSystem.entity.PayslipEntity;

import com.example.LeaveManagementSystem.service.LeaveService;
import com.example.LeaveManagementSystem.service.PayslipService;
import org.springframework.stereotype.Service;

import com.example.LeaveManagementSystem.repository.PayslipRep;
import com.example.LeaveManagementSystem.utils.ErrorUtil;

import lombok.extern.slf4j.Slf4j;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;

@Service
@Slf4j
public class PayslipServiceImpl implements PayslipService {
    private final PayslipRep payslipRepository;
    private final LeaveService leaveService;

    public PayslipServiceImpl(PayslipRep payslipRepository, LeaveService leaveService) {
        this.payslipRepository = payslipRepository;
        this.leaveService = leaveService;
    }

    public ErrorUtil<String, String> save(CreatePayslipDTO dto) {
        var employee = this.leaveService.isEmployeeExists(dto.employeeId());
        if (!employee) {
            return new ErrorUtil<>(false, "employee with UUID is not found", null);
        }

        var emp = EmployeeEntity.builder()
                .id(dto.employeeId()).build();

        var payslip = PayslipEntity.builder()
                .file(dto.file())
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

    @Override
    public ErrorUtil<String, ByteArrayOutputStream> generatePDF(
            CreatePayslipPDFDTO dto) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
            resolver.setPrefix("/templates/pdf/");
            resolver.setSuffix(".html");

            TemplateEngine engine = new TemplateEngine();
            engine.setTemplateResolver(resolver);

            Context context = new Context();
            context.setVariable("name", "Hello Seaman !!");
            String output = engine.process("payslip.html", context);

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(output);
            renderer.getFontResolver().addFont("roboto.ttf", true);
            renderer.layout();
            renderer.createPDF(stream);
            stream.close();
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ErrorUtil<>(false, e.getMessage(), null);
        }
        return new ErrorUtil<>(true, null, stream);
    }
}
