package com.example.LeaveManagementSystem.serviceImpl;

import com.example.LeaveManagementSystem.entity.EmployeeEntity;
import com.example.LeaveManagementSystem.repository.EmployeeRepo;
import com.example.LeaveManagementSystem.service.ApraisalServiceInter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Service
@Slf4j
public class AppraisalServiceImpl implements ApraisalServiceInter {

    @Autowired
    private EmployeeRepo employeeRepository;

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

        // Assuming getHireDate() returns Date
        Date hireDateDate = employee.getHireDate();
        LocalDate hireDate = hireDateDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate currentDate = LocalDate.now();

        // Calculate the number of years since the employee was hired
        long yearsWorked = ChronoUnit.YEARS.between(hireDate, currentDate);
        log.info("Employee ID {} has worked for {} years", employee.getId(), yearsWorked);
        return yearsWorked >= 1;
    }

    private void sendAppraisalEmail(EmployeeEntity employee) {
        try {
            double appraisalPercentage = 10.0; // Example value
            double newSalary = calculateNewSalary(employee.getEmployeeSalary(), appraisalPercentage);
            String effectiveDate = LocalDate.now().toString();
            log.info("Calculated new salary for employee ID {}: {}", employee.getId(), newSalary);

            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost("smtp.gmail.com");
            mailSender.setPort(587);
            mailSender.setUsername("devapriyanstephen24@gmail.com");
            mailSender.setPassword("gwsphcbdsbjgolll");

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true"); // Ensure STARTTLS is enabled
            props.put("mail.debug", "true");

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
        // Calculate new salary based on current salary and appraisal percentage
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
