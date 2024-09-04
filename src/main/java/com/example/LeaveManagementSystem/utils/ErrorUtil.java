package com.example.LeaveManagementSystem.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorUtil<T, D> {
    private Boolean ok;
    private T message;
    private D data;

    public Boolean isOk() {
        return this.ok;
    }
}
