package com.example.LeaveManagementSystem.service;

import com.example.LeaveManagementSystem.dto.EmployeeResponseDTO;
import com.example.LeaveManagementSystem.dto.LeaveResponseDTO;
import com.example.LeaveManagementSystem.entity.EmployeeEntity;
import com.example.LeaveManagementSystem.entity.LeaveEntity;
import com.example.LeaveManagementSystem.entity.OrganizationEntity;
import com.example.LeaveManagementSystem.entity.RejectLeaveEntity;
import com.example.LeaveManagementSystem.exceptions.UserNotFoundException;
import com.example.LeaveManagementSystem.entity.*;
import com.example.LeaveManagementSystem.response.ApiResponse;
import com.example.LeaveManagementSystem.utils.ErrorUtil;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.UUID;

public interface LeaveService {

    public ApiResponse<OrganizationEntity> saveOrganization(OrganizationEntity oentity, boolean isUpdate);

    public boolean organizationEmailExists(String email);

    public boolean checkLocation(String location);

    public ApiResponse<EmployeeResponseDTO> saveEmployee(EmployeeEntity entity, boolean isUpdate);

    public boolean isEmailExists(String email);

    public boolean isOrganizationExists(UUID id);

    public ApiResponse<LeaveResponseDTO> applyLeave(LeaveEntity entity);

    public boolean isEmployeeExists(UUID id);

    ResponseEntity<ApiResponse<OrganizationEntity>> deleteOrganizationID(UUID id);

    ResponseEntity<ApiResponse<EmployeeEntity>> deleteEmployeeById(UUID id);

    ResponseEntity<ApiResponse<LeaveEntity>> deleteLeave(UUID id);

    boolean hasEnoughLeaves(UUID id, int requiredDays);

    ErrorUtil<String, String> acceptLeave(AcceptLeave entity);

    ErrorUtil<String, String> rejectLeave(RejectLeaveEntity entity);
}
