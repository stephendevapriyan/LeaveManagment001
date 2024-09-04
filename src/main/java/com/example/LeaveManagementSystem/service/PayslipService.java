package com.example.LeaveManagementSystem.service;

import com.example.LeaveManagementSystem.dto.CreatePayslipDTO;
import com.example.LeaveManagementSystem.dto.CreatePayslipPDFDTO;
import com.example.LeaveManagementSystem.utils.ErrorUtil;
import java.io.*;

public interface PayslipService {
    ErrorUtil<String, String> save(CreatePayslipDTO dto);

    ErrorUtil<String, ByteArrayOutputStream> generatePDF(CreatePayslipPDFDTO dto);
}
