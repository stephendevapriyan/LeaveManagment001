package com.example.LeaveManagementSystem.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.UUID;

public interface AuthService {
    String generatePassword(UUID id, String password);

    boolean hasUserSetPassword(String email) throws UsernameNotFoundException;
}