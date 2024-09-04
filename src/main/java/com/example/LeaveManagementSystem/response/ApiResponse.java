package com.example.LeaveManagementSystem.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ApiResponse<T> {

    String message;
    Integer status;
    T data;
}
