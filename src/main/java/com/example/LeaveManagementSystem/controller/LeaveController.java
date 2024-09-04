package com.example.LeaveManagementSystem.controller;

import com.example.LeaveManagementSystem.dto.EmployeeResponseDTO;
import com.example.LeaveManagementSystem.dto.LeaveResponseDTO;
import com.example.LeaveManagementSystem.entity.AcceptLeaveEntity;
import com.example.LeaveManagementSystem.entity.EmployeeEntity;
import com.example.LeaveManagementSystem.entity.LeaveEntity;
import com.example.LeaveManagementSystem.entity.OrganizationEntity;
import com.example.LeaveManagementSystem.entity.RejectLeaveEntity;
import com.example.LeaveManagementSystem.response.ApiResponse;
import com.example.LeaveManagementSystem.serviceImpl.CustomUserDetailServiceImpl;
import com.example.LeaveManagementSystem.service.LeaveService;
import com.example.LeaveManagementSystem.utils.ErrorUtil;

import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class LeaveController {

    @Autowired
    private LeaveService service;

    @Autowired
    private CustomUserDetailServiceImpl userDetailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/organization")
    public ResponseEntity<ApiResponse<OrganizationEntity>> addOrganization(@RequestBody OrganizationEntity entity) {
        ApiResponse<OrganizationEntity> response = service.saveOrganization(entity);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
    }

    @PostMapping("/employee")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> addEmployee(@RequestBody EmployeeEntity entity) {
        ApiResponse<EmployeeResponseDTO> response = service.saveEmployee(entity);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
    }

    @PostMapping("/leave")
    public ResponseEntity<ApiResponse<LeaveResponseDTO>> leaveApply(@RequestBody LeaveEntity entity) {
        ApiResponse<LeaveResponseDTO> response = service.applyLeave(entity);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));

    }


    @GetMapping("/login")
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password) {
        try {

            UserDetails userDetails = userDetailService.loadUserByUsername(email);
            String userPassword=passwordEncoder.encode(password);

            if (passwordEncoder.matches(password, userDetails.getPassword())) {

                return ResponseEntity.status(HttpStatus.OK).body("Login successful");
            }
            else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }


    @PostMapping("/passwords")
    public String createPassword(@RequestParam UUID id, String password){
        return service.generatePassword(id,password);
    }

    @PostMapping("/accept-leave")
    public ResponseEntity<?> acceptLeave(@RequestBody AcceptLeaveEntity acceptLeave) {
        ErrorUtil<String, String> res = service.acceptLeave(acceptLeave);
        if (!res.isOk()) {
            return new ResponseEntity<ApiResponse<?>>(
                    new ApiResponse<String>("error", HttpStatus.BAD_GATEWAY.value(), res.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<ApiResponse<?>>(new ApiResponse<String>("done", 200, res.getData()),
                HttpStatus.OK);
    }

    @PostMapping("/reject-leave")
    @SneakyThrows
    public ResponseEntity<?> rejectLeave(@RequestBody RejectLeaveEntity rejectLeave) {
        ErrorUtil<String, String> res = service.rejectLeave(rejectLeave);
        if (!res.isOk()) {
            return new ResponseEntity<ApiResponse<?>>(
                    new ApiResponse<String>("error", HttpStatus.BAD_GATEWAY.value(), res.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<ApiResponse<?>>(new ApiResponse<String>("done", 200, res.getData()),
                HttpStatus.OK);
    }

    @DeleteMapping("/deleteorg")
    ResponseEntity<ApiResponse<OrganizationEntity>> deleteOLM(@RequestParam(name = "id") UUID id) {

        return service.deleteOrganizationID(id);
    }

    @DeleteMapping("/deleteemployee")
    ResponseEntity<ApiResponse<EmployeeEntity>> employeedelete(@RequestParam(name = "id") UUID id) {
        return service.deleteEmployeeById(id);
    }

    @DeleteMapping("/deleteleave")
    ResponseEntity<ApiResponse<LeaveEntity>> deleteLeave(@RequestParam(name = "id") UUID id) {

        return service.deleteLeave(id);
    }

}
