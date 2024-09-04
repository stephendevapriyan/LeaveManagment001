package com.example.LeaveManagementSystem.serviceImpl;

import com.example.LeaveManagementSystem.entity.EmployeeEntity;
import com.example.LeaveManagementSystem.repository.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private EmployeeRepo employeeRepo;



    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<EmployeeEntity> employee =employeeRepo.findByEmail(email);
        if(employee.isPresent()){
            var employeeData= employee.get();
            return User.builder()
                    .username(employeeData.getEmail())
                    .password(employeeData.getEncryptedPassword())
                    .build();
        }
        else{
            throw new UsernameNotFoundException("invalid email id");
        }

    }
}
