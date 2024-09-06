package com.example.LeaveManagementSystem.service;

import java.util.UUID;

public interface ApraisalServiceInter {



    void sendAppraisalEmailToEmployee(UUID UUID);


    void sendAppraisalEmailsToAll();
}
