package com.example.LeaveManagementSystem.controller;



import com.example.LeaveManagementSystem.service.ApraisalServiceInter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppraisalController {

    @Autowired
    private ApraisalServiceInter service;

    @GetMapping ("/send-appraisal-emails")
    public ResponseEntity<String> sendAppraisalEmailsToAll() {
        System.out.println("stephen");
        service.sendAppraisalEmailsToAll();
        return ResponseEntity.ok("Appraisal emails have been sent to all eligible employees.");
    }
}

