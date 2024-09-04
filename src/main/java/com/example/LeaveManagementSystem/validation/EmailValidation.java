package com.example.LeaveManagementSystem.validation;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EmailValidation {

    private final String regex="^[a-zA-Z][a-zA-Z0-9._-]+@gmail\\.com$";
    Pattern pattern= Pattern.compile(regex,Pattern.CASE_INSENSITIVE);

    public boolean isEmailValid(String email){
        if(email.isEmpty() || email==null){
            return false;
        }

        Matcher matcher=pattern.matcher(email);
        return matcher.matches();
    }
}
