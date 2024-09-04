package com.example.LeaveManagementSystem.exceptions;

public class UserNotFoundException extends Exception {
    public UserNotFoundException() {
        super("user not found");
    }
}
