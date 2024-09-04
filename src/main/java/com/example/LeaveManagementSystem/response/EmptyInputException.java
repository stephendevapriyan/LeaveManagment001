package com.example.LeaveManagementSystem.response;

public class EmptyInputException extends RuntimeException{
    public EmptyInputException(String message) {
        super(message);
    }
}
