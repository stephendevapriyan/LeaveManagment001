package com.example.LeaveManagementSystem.controller;



import com.example.LeaveManagementSystem.service.ApraisalServiceInter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Slf4j
public class AppraisalController {

    @Autowired
    private ApraisalServiceInter service;


    @GetMapping("/send-appraisal-emails")
    public ResponseEntity<String> sendAppraisalEmails(@RequestParam(value = "id", required = false) UUID id) {
        try {
            if (id != null) {
                // Send appraisal email to a specific employee
                service.sendAppraisalEmailToEmployee(id);
                return ResponseEntity.ok("Appraisal email has been sent to employee ID " + id);
            } else {
                // Send appraisal emails to all employees
                service.sendAppraisalEmailsToAll();
                return ResponseEntity.ok("Appraisal emails have been sent to all eligible employees.");
            }
        } catch (Exception e) {
            // Log the exception and return an appropriate response
            log.error("Failed to send appraisal emails: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send appraisal emails.");
        }
    }
}


