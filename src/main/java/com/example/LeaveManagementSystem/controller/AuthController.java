package com.example.LeaveManagementSystem.controller;

import com.example.LeaveManagementSystem.service.AuthService;
import com.example.LeaveManagementSystem.service.LeaveService;
import com.example.LeaveManagementSystem.serviceImpl.CustomUserDetailServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private AuthService service;

    @Autowired
    private CustomUserDetailServiceImpl userDetailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password) {
        try {
            if (!service.hasUserSetPassword(email)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user has not set the password");
            }

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
    public ResponseEntity<String> createPassword(@RequestParam UUID id){
        try {
            String password = service.generatePassword(id);
            return ResponseEntity.status(HttpStatus.OK).body(password);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }
}
