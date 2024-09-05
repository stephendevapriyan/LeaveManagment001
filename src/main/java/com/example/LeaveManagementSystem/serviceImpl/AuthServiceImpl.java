package com.example.LeaveManagementSystem.serviceImpl;

import com.example.LeaveManagementSystem.entity.EmployeeEntity;
import com.example.LeaveManagementSystem.repository.EmployeeRepo;
import com.example.LeaveManagementSystem.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private EmployeeRepo erepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public boolean hasUserSetPassword(String email) throws UsernameNotFoundException {
        Optional<EmployeeEntity> employee = erepository.findByEmail(email);
        if (employee.isEmpty()) throw new UsernameNotFoundException("user not found");
        return employee.get().getPassword() != null && !employee.get().getPassword().isBlank();
    }

    @Override
    public String generatePassword(UUID id, String password) {
        if (password.length() < 8) {
            return "password length is short";
        }
        if (!Pattern.compile("[A-Z]").matcher(password).find()) {
            return "Password should contain atleast one uppercase character";
        }
        if (!Pattern.compile("[a-z]").matcher(password).find()) {
            return "Password should contain atleast one lowecase character";
        }
        if (!Pattern.compile("[0-9]").matcher(password).find()) {
            return "Password should contain atleast one character";
        }
        if (!Pattern.compile("[^a-zA-Z0-9]").matcher(password).find()) {
            return "There is no shecial character";
        }

        EmployeeEntity employeeEntity = erepository.findById(id).get();
        employeeEntity.setPassword(password);
        String encrypting = passwordEncoder.encode(password);
        employeeEntity.setEncryptedPassword(encrypting);
        erepository.save(employeeEntity);
        return "password generated successfully";
    }
}
