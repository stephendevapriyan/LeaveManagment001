package com.example.LeaveManagementSystem.serviceImpl;

import com.example.LeaveManagementSystem.entity.EmployeeEntity;
import com.example.LeaveManagementSystem.repository.EmployeeRepo;
import com.example.LeaveManagementSystem.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

@Slf4j
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

    private String getRandomCharacters(String s, int length) {
        Random random = new Random();
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < length; ++i) {
            res.append(s.charAt(random.nextInt(0, s.length())));
        }
        return res.toString();
    }

    @Override
    public String generatePassword(UUID id) {
        if (erepository.findById(id).isEmpty())
            throw new UsernameNotFoundException("user not found");

        EmployeeEntity employeeEntity = erepository.findById(id).get();

        String firstHalf = getRandomCharacters(employeeEntity.getFirstname().substring(0, 3), 3);
        String secondHalf = String.valueOf(employeeEntity.getHireDate().getYear());
        String specialCharacters = "@$#^*(";
        String thirdHalf = getRandomCharacters(specialCharacters, 3);
        String finalPassword = firstHalf + secondHalf + thirdHalf;

        employeeEntity.setPassword(finalPassword);
        String encrypting = passwordEncoder.encode(finalPassword);
        employeeEntity.setEncryptedPassword(encrypting);
        erepository.save(employeeEntity);

        // Set up the mail sender
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername(employeeEntity.getEmail()); // Use the employee email as username
        mailSender.setPassword("gwsphcbdsbjgolll"); // Securely handle this password

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // Ensure STARTTLS is enabled
        props.put("mail.debug", "true");

        // Create and configure the email message
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("admin@canvendor.com");
        message.setTo(employeeEntity.getEmail());
        message.setSubject("Leave Request Received");
        message.setText(String.format("""
                Hi, For logging into your account use the following credentials
                username: %s,
                password: %s
                """, employeeEntity.getEmail(), finalPassword));

        // Send the email
        try {
            mailSender.send(message);
            log.info("Leave request email sent to {}", employeeEntity.getEmail());
        } catch (MailException e) {
            log.error("Failed to send email to {}: {}", employeeEntity.getEmail(), e.getMessage());
        }
        return "password generated successfully";
    }
}
