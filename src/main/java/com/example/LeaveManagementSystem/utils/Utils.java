package com.example.LeaveManagementSystem.utils;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

@Component
public class Utils {
    public LocalDate stringToLocalDate(String date) {
        return LocalDate.parse(date);
    }
}
