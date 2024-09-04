package com.example.LeaveManagementSystem.validation;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MobileNoValidation {

    private final String regex = "^(\\+91[\\s-]?)?[6-9]\\d{9}$";

    Pattern pattern= Pattern.compile(regex);

    public boolean isNumberValid(String number){
        if(number.isEmpty() || number==null){
            return false;
        }

        Matcher matcher=pattern.matcher(number);
        return matcher.matches();
    }
}
