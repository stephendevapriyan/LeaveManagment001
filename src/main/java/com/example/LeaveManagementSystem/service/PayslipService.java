package com.example.LeaveManagementSystem.service;

import com.example.LeaveManagementSystem.dto.CreatePayslipDTO;
import com.example.LeaveManagementSystem.dto.CreatePayslipPDFDTO;
import com.example.LeaveManagementSystem.entity.PayslipEntity;
import com.example.LeaveManagementSystem.utils.ErrorUtil;
import java.io.*;
import java.util.List;

public interface PayslipService {
    ErrorUtil<String, String> save(CreatePayslipDTO dto);

    ErrorUtil<String, ByteArrayOutputStream> generatePDF(CreatePayslipPDFDTO dto);


    List<byte[]> getAllPdfFiles();
}
