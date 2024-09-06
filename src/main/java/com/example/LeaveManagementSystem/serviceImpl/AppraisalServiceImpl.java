package com.example.LeaveManagementSystem.serviceImpl;

import com.example.LeaveManagementSystem.entity.EmployeeEntity;
import com.example.LeaveManagementSystem.repository.EmployeeRepo;
import com.example.LeaveManagementSystem.service.ApraisalServiceInter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class AppraisalServiceImpl implements ApraisalServiceInter {
    @Autowired
    private EmployeeRepo employeeRepository;

    @Autowired
    private JavaMailSender mailSender = createMailSender();

    private JavaMailSender createMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername("devapriyanstephen24@gmail.com");
        mailSender.setPassword("gwsphcbdsbjgolll");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }

    public void sendAppraisalEmailToEmployee(UUID id) {
        try {
            Optional<EmployeeEntity> optionalEmployee = employeeRepository.findById(id);
            if (optionalEmployee.isPresent()) {
                EmployeeEntity employee = optionalEmployee.get();
                if (shouldSendAppraisal(employee)) {
                    sendAppraisalEmail(employee);
                } else {
                    throw new RuntimeException("Employee is not eligible for appraisal.");
                }
            } else {
                log.warn("Employee with ID {} not found.", id);
            }
        } catch (Exception e) {
            log.error("Error sending appraisal email: {}", e.getMessage());
            throw e;  // Rethrow the exception after logging
        }
    }

    public void sendAppraisalEmailsToAll() {
        List<EmployeeEntity> employees = employeeRepository.findAll();
        for (EmployeeEntity employee : employees) {
            try {
                if (shouldSendAppraisal(employee)) {
                    sendAppraisalEmail(employee);
                }
            } catch (Exception e) {
                log.error("Failed to process appraisal for employee ID {}: {}", employee.getId(), e.getMessage());
            }
        }
    }

    private boolean shouldSendAppraisal(EmployeeEntity employee) {
        if (employee == null || employee.getHireDate() == null) {
            log.warn("Employee or hire date is null for employee ID {}", employee != null ? employee.getId() : "Unknown");
            return false;
        }

        Date hireDateDate = employee.getHireDate();
        LocalDate hireDate = hireDateDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate currentDate = LocalDate.now();
        long yearsWorked = ChronoUnit.YEARS.between(hireDate, currentDate);

        return yearsWorked >= 1;
    }

    private void sendAppraisalEmail(EmployeeEntity employee) {
        try {
            double appraisalPercentage = 10.0; // Example value
            double newSalary = calculateNewSalary(employee.getEmployeeSalary(), appraisalPercentage);
            String effectiveDate = LocalDate.now().toString();
            log.info("Calculated new salary for employee ID {}: {}", employee.getId(), newSalary);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("devapriyanstephen24@gmail.com");
            message.setTo(employee.getEmail());
            log.info("Sending appraisal email to {}", employee.getEmail());
            message.setSubject("Appraisal Allotment Notification");
            message.setText(buildEmailContent(employee, appraisalPercentage, newSalary, effectiveDate));

            mailSender.send(message);
            log.info("Appraisal email sent to {}", employee.getEmail());
        } catch (Exception e) {
            log.error("Failed to send appraisal email to employee ID {}: {}", employee.getId(), e.getMessage());
        }
    }

    private double calculateNewSalary(double currentSalary, double appraisalPercentage) {
        return currentSalary + (currentSalary * appraisalPercentage / 100);
    }

    private String buildEmailContent(EmployeeEntity employee, double appraisalPercentage, double newSalary, String effectiveDate) {
        return String.format(
                "Dear %s %s,\n\n" +
                        "Congratulations on completing another successful year with us! We are thrilled to inform you that your efforts and dedication have been recognized. " +
                        "As a token of our appreciation, we are pleased to announce your appraisal details as follows:\n\n" +
                        "Job Title: %s\n" +
                        "Appraisal Percentage: %.2f%%\n" +
                        "New Salary: %.2f per annum\n" +
                        "Effective Date: %s\n\n" +
                        "We are confident that you will continue to excel in your role and contribute to our shared success. " +
                        "Thank you for being an invaluable part of our team.\n\n" +
                        "Best Regards,\n" +
                        "HR Department\n" +
                        "Canvendor",
                employee.getFirstname(),
                employee.getLastname(),
                employee.getJobTitle(),
                appraisalPercentage,
                newSalary,
                effectiveDate
        );
    }

}
