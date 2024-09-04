package com.example.LeaveManagementSystem.serviceImpl;

import com.example.LeaveManagementSystem.dto.EmployeeResponseDTO;
import com.example.LeaveManagementSystem.dto.LeaveResponseDTO;
import com.example.LeaveManagementSystem.entity.AcceptLeaveEntity;
import com.example.LeaveManagementSystem.entity.EmployeeEntity;
import com.example.LeaveManagementSystem.entity.LeaveEntity;
import com.example.LeaveManagementSystem.entity.LeaveStatus;
import com.example.LeaveManagementSystem.entity.OrganizationEntity;
import com.example.LeaveManagementSystem.entity.RejectLeaveEntity;
import com.example.LeaveManagementSystem.exceptions.UserNotFoundException;
import com.example.LeaveManagementSystem.repository.AcceptLeaveEntityRepo;
import com.example.LeaveManagementSystem.repository.EmployeeRepo;
import com.example.LeaveManagementSystem.repository.LeaveRepo;
import com.example.LeaveManagementSystem.repository.OrganizationRepo;
import com.example.LeaveManagementSystem.repository.RejectLeaveEntityRepo;
import com.example.LeaveManagementSystem.response.ApiResponse;
import com.example.LeaveManagementSystem.service.LeaveService;
import com.example.LeaveManagementSystem.utils.ErrorUtil;

import com.example.LeaveManagementSystem.validation.EmailValidation;
import com.example.LeaveManagementSystem.validation.MobileNoValidation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
public class LeaveServiceImpl implements LeaveService {

    @Autowired
    private OrganizationRepo orepo;
    @Autowired
    private EmployeeRepo erepository;
    @Autowired
    private LeaveRepo leaverepo;
    @Autowired
    private AcceptLeaveEntityRepo acceptLeaveEntityRepo;
    @Autowired
    private RejectLeaveEntityRepo rejectLeaveEntityRepo;
    @Autowired
    private EmailValidation emailValidation;
    @Autowired
    private MobileNoValidation mobileNoValidation;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    JavaMailSender mailSender;

    // save organization
    @Override
    public ApiResponse<OrganizationEntity> saveOrganization(OrganizationEntity oentity) {
        log.info("save organization method started");
        try {
            if (organizationEmailExists(oentity.getEmail()) && checkLocation(oentity.getLocation())) {
                log.warn("already registered company with give email and location");
                return ApiResponse.<OrganizationEntity>builder()
                        .message("Company is already registered for the given email and location")
                        .status(HttpStatus.BAD_REQUEST.value())
                        .data(null)
                        .build();
            }
            if (!emailValidation.isEmailValid(oentity.getEmail())) {
                log.warn("invalid email id ");
                return ApiResponse.<OrganizationEntity>builder()
                        .message("invalid email id please check")
                        .status(HttpStatus.BAD_REQUEST.value())
                        .data(null)
                        .build();
            }
            if (!mobileNoValidation.isNumberValid(oentity.getContactNumber())) {
                log.warn("invalid mobile no");
                return ApiResponse.<OrganizationEntity>builder()
                        .message("invalid mobile no please check")
                        .status(HttpStatus.BAD_REQUEST.value())
                        .data(null)
                        .build();
            }

            OrganizationEntity savedEntity = orepo.save(oentity);
            log.info("successfully saved organization");
            return ApiResponse.<OrganizationEntity>builder()
                    .message("successfully saved organization")
                    .status(HttpStatus.OK.value())
                    .data(savedEntity)
                    .build();
        } catch (Exception e) {
            log.error("invalid input please check" + e.getMessage());
            return ApiResponse.<OrganizationEntity>builder()
                    .message("invalid input please check" + e.getMessage())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .data(null)
                    .build();
        } finally {
            log.info("save organization method completed");
        }
    }

    @Override
    public boolean organizationEmailExists(String email) {
        log.info("checking organization email method started");
        if (orepo.findByEmail(email).isPresent()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean checkLocation(String location) {
        log.info("checking organization location method started");
        if (orepo.findByLocation(location).isPresent()) {
            return true;
        }
        return false;
    }

    // save employee
    @Override
    public ApiResponse<EmployeeResponseDTO> saveEmployee(EmployeeEntity entity) {
        log.info("saving employee method started");
        try {
            if (isEmailExists(entity.getEmail())) {
                log.warn("Email id already exists");
                return ApiResponse.<EmployeeResponseDTO>builder()
                        .message("Email id already exists")
                        .status(HttpStatus.BAD_REQUEST.value())
                        .data(null)
                        .build();
            }
            if (entity.getOrganization() == null || !isOrganizationExists(entity.getOrganization().getId())) {
                log.warn("Invalid organization");
                return ApiResponse.<EmployeeResponseDTO>builder()
                        .message("Invalid organization")
                        .status(HttpStatus.BAD_REQUEST.value())
                        .data(null)
                        .build();
            }
            if (!emailValidation.isEmailValid(entity.getEmail())) {
                log.warn("invalid email id ");
                return ApiResponse.<EmployeeResponseDTO>builder()
                        .message("invalid email id please check")
                        .status(HttpStatus.BAD_REQUEST.value())
                        .data(null)
                        .build();
            }
            if (!mobileNoValidation.isNumberValid(entity.getPhoneNumber())) {
                log.warn("invalid mobile no ");
                return ApiResponse.<EmployeeResponseDTO>builder()
                        .message("invalid mobile no")
                        .status(HttpStatus.BAD_REQUEST.value())
                        .data(null)
                        .build();
            }

            EmployeeEntity savedEntity = erepository.save(entity);
            log.info("Successfully saved employee");

            EmployeeResponseDTO dto = new EmployeeResponseDTO();

            dto.setId(entity.getId());
            dto.setFirstname(entity.getFirstname());
            dto.setLastname(entity.getLastname());
            dto.setEmail(entity.getEmail());
            dto.setRole(entity.getRole());
            dto.setPhoneNumber(entity.getPhoneNumber());
            dto.setHireDate(entity.getHireDate());
            dto.setJobTitle(entity.getJobTitle());
            dto.setOrganizationId(entity.getOrganization().getId());
            dto.setActive(entity.isActive());
            dto.setCreatedAt(entity.getCreatedAt());
            dto.setUpdatedAt(entity.getUpdatedAt());

            return ApiResponse.<EmployeeResponseDTO>builder()
                    .status(HttpStatus.OK.value())
                    .message("Successfully saved")
                    .data(dto)
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error("invalid input please check");
            return ApiResponse.<EmployeeResponseDTO>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("invalid input please check")
                    .data(null)
                    .build();
        } finally {
            log.info("save employee method completed");
        }
    }

    @Override
    public boolean isEmailExists(String email) {
        log.info(" employee email exists method started");
        if (erepository.findByEmail(email).isPresent()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isOrganizationExists(UUID id) {
        log.info(" organization exists method started");
        if (orepo.findById(id).isPresent()) {
            return true;
        }
        return false;
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

    // apply leave

    @Override
    public ApiResponse<LeaveResponseDTO> applyLeave(LeaveEntity entity) {

        UUID employeeid = entity.getEmployee().getId();
        EmployeeEntity employeeEntity = erepository.findById(employeeid).get();

        Period difference = Period.between(entity.getStartDate(), entity.getEndDate());
        int noOfDays = difference.getDays() + 1;
        log.info("apply leave method started");
        try {
            // Check if employee exists
            if (!isEmployeeExists(entity.getEmployee().getId())) {
                log.warn("Invalid employee ID");
                return ApiResponse.<LeaveResponseDTO>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Invalid employee ID")
                        .data(null)
                        .build();
            }
            if (employeeEntity.getLeaveCount() < noOfDays) {
                return ApiResponse.<LeaveResponseDTO>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Insufficient leave balance")
                        .data(null)
                        .build();
            }
            List<LeaveEntity> matchedDates = leaverepo.findLeavesByEmployeeAndDates(employeeid, entity.getStartDate(),
                    entity.getEndDate());
            if (!matchedDates.isEmpty()) {
                return ApiResponse.<LeaveResponseDTO>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Leave dates overlap with existing leave records")
                        .data(null)
                        .build();
            }
            // Save the leave entity
            LeaveEntity saved = leaverepo.save(entity);
            log.info("Successfully applied leave");
            Optional<EmployeeEntity> byId = erepository.findById(saved.getEmployee().getId());

            String employeeEmail = byId.get().getEmail();
            String assigningEmail = saved.getAssigningEmail();

            // Check if email addresses are valid
            if (employeeEmail == null || employeeEmail.isEmpty()) {
                log.warn("Employee email is null or empty");
                throw new IllegalArgumentException("Employee email is invalid");
            }
            if (assigningEmail == null || assigningEmail.isEmpty()) {
                log.warn("Assigning email is null or empty");
                throw new IllegalArgumentException("Assigning email is invalid");
            }

            // Set up the mail sender
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost("smtp.gmail.com");
            mailSender.setPort(587);
            mailSender.setUsername(employeeEmail); // Use the employee email as username
            mailSender.setPassword("gwsphcbdsbjgolll"); // Securely handle this password

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true"); // Ensure STARTTLS is enabled
            props.put("mail.debug", "true");

            // Create and configure the email message
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(employeeEmail);
            message.setTo(assigningEmail);
            message.setSubject("Leave Request Received");
            message.setText("Your leave request for " + saved.getLeaveType() +
                    " from " + saved.getStartDate() +
                    " to " + saved.getEndDate() +
                    " has been received and is pending approval.");

            // Send the email
            try {
                mailSender.send(message);
                log.info("Leave request email sent to {}", assigningEmail);
            } catch (MailException e) {
                log.error("Failed to send email to {}: {}", assigningEmail, e.getMessage());
            }

            // Prepare the response DTO
            LeaveResponseDTO dto = new LeaveResponseDTO();
            dto.setId(saved.getId());
            dto.setEmployeeId(saved.getEmployee().getId());
            dto.setStartDate(saved.getStartDate());
            dto.setEndDate(saved.getEndDate());
            dto.setLeaveType(saved.getLeaveType());
            dto.setStatus(saved.getStatus());
            dto.setRequestDate(saved.getRequestDate());
            dto.setLeaveReason(saved.getLeaveReason());
            dto.setApprovedDate(saved.getApprovedDate());
            dto.setCreatedAt(saved.getCreatedAt());
            dto.setUpdatedAt(saved.getUpdatedAt());

            // Return a successful response
            return ApiResponse.<LeaveResponseDTO>builder()
                    .status(HttpStatus.OK.value())
                    .message("Successfully applied leave")
                    .data(dto)
                    .build();
        } catch (IllegalArgumentException e) {
            log.error("Validation failed: {}", e.getMessage());
            return ApiResponse.<LeaveResponseDTO>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .data(null)
                    .build();
        } catch (Exception e) {
            log.error("Exception occurred: ", e);
            return ApiResponse.<LeaveResponseDTO>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("An unexpected error occurred")
                    .data(null)
                    .build();
        } finally {
            log.info("Apply leave method completed");
        }
    }

    @Override
    public boolean isEmployeeExists(UUID id) {
        log.info("employee exists method started");
        if (erepository.findById(id).isPresent()) {
            return true;
        }
        return false;
    }

    // stephenDevepriyan

    public ResponseEntity<ApiResponse<OrganizationEntity>> deleteOrganizationID(UUID id) {
        log.info("Attempting to delete organization with ID: {}", id);

        try {
            // Fetch the organization entity by ID
            Optional<OrganizationEntity> optionalOrganization = orepo.findById(id);

            if (optionalOrganization.isEmpty()) {
                String errorMessage = "The organization with ID " + id + " was not found.";
                log.error(errorMessage);
                ApiResponse<OrganizationEntity> response = ApiResponse.<OrganizationEntity>builder()
                        .message(errorMessage)
                        .status(HttpStatus.NOT_FOUND.value())
                        .data(null)
                        .build();
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            OrganizationEntity organizationEntity = optionalOrganization.get();

            // Check if the organization is already marked as deleted
            if (organizationEntity.isDelete()) {
                String errorMessage = "The organization with ID " + id + " is already marked as deleted.";
                log.warn(errorMessage);
                ApiResponse<OrganizationEntity> response = ApiResponse.<OrganizationEntity>builder()
                        .message(errorMessage)
                        .status(HttpStatus.CONFLICT.value())
                        .data(null)
                        .build();
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }

            // Mark the organization entity as deleted and update the deletion timestamp
            organizationEntity.setDelete(true);
            organizationEntity.setDeletedAt(LocalDateTime.now());

            // Save the changes to the repository
            orepo.save(organizationEntity);

            ApiResponse<OrganizationEntity> response = ApiResponse.<OrganizationEntity>builder()
                    .message("Organization successfully marked as deleted")
                    .status(HttpStatus.OK.value())
                    .data(organizationEntity)
                    .build();

            log.info("Successfully marked organization with ID {} as deleted", id);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException ex) {
            log.error("RuntimeException occurred while deleting organization with ID {}: {}", id, ex.getMessage(), ex);
            ApiResponse<OrganizationEntity> response = ApiResponse.<OrganizationEntity>builder()
                    .message("An error occurred while processing the request.")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .data(null)
                    .build();
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {
            log.error("Exception occurred while deleting organization with ID {}: {}", id, ex.getMessage(), ex);
            ApiResponse<OrganizationEntity> response = ApiResponse.<OrganizationEntity>builder()
                    .message("An unexpected error occurred while processing the request.")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .data(null)
                    .build();
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<ApiResponse<EmployeeEntity>> deleteEmployeeById(UUID id) {
        log.info("Attempting to delete employee with ID: {}", id);

        try {
            // Fetch the employee entity by ID
            Optional<EmployeeEntity> optionalEmployee = erepository.findById(id);

            // Check if the employee entity is present
            if (optionalEmployee.isEmpty()) {
                String errorMessage = "The employee with ID " + id + " was not found.";
                log.error(errorMessage);
                ApiResponse<EmployeeEntity> response = ApiResponse.<EmployeeEntity>builder()
                        .message(errorMessage)
                        .status(HttpStatus.NOT_FOUND.value())
                        .data(null)
                        .build();
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            EmployeeEntity employeeEntity = optionalEmployee.get();

            // Check if the employee is already marked as deleted
            if (employeeEntity.isDelete()) {
                String errorMessage = "The employee with ID " + id + " is already marked as deleted.";
                log.warn(errorMessage);
                ApiResponse<EmployeeEntity> response = ApiResponse.<EmployeeEntity>builder()
                        .message(errorMessage)
                        .status(HttpStatus.CONFLICT.value())
                        .data(null)
                        .build();
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }

            // Mark the employee entity as deleted and update the deletion timestamp
            employeeEntity.setDelete(true);
            employeeEntity.setDeletedAt(LocalDateTime.now());

            // Save the changes to the repository
            erepository.save(employeeEntity);

            ApiResponse<EmployeeEntity> response = ApiResponse.<EmployeeEntity>builder()

                    .message("Employee successfully marked as deleted")
                    .status(HttpStatus.OK.value())
                    .data(employeeEntity)
                    .build();

            log.info("Successfully marked employee with ID {} as deleted", id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NullPointerException e) {
            log.error("NullPointerException occurred while deleting employee with ID {}: {}", id, e.getMessage(), e);
            ApiResponse<EmployeeEntity> response = ApiResponse.<EmployeeEntity>builder()
                    .message("A null pointer exception occurred while processing the request.")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .data(null)
                    .build();
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            log.error("RuntimeException occurred while deleting employee with ID {}: {}", id, e.getMessage(), e);
            ApiResponse<EmployeeEntity> response = ApiResponse.<EmployeeEntity>builder()

                    .message("An error occurred while processing the request.")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .data(null)
                    .build();
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Exception occurred while deleting employee with ID {}: {}", id, e.getMessage(), e);
            ApiResponse<EmployeeEntity> response = ApiResponse.<EmployeeEntity>builder()
                    .message("An unexpected error occurred while processing the request.")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .data(null)
                    .build();
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<ApiResponse<LeaveEntity>> deleteLeave(UUID id) {
        log.info("Attempting to delete leave with ID: {}", id);

        try {
            // Fetch the leave entity by ID
            Optional<LeaveEntity> optionalLeave = leaverepo.findById(id);

            if (optionalLeave.isEmpty()) {
                String errorMessage = "The leave with ID " + id + " was not found.";
                log.error(errorMessage);
                ApiResponse<LeaveEntity> response = ApiResponse.<LeaveEntity>builder()
                        .message(errorMessage)
                        .status(HttpStatus.NOT_FOUND.value())
                        .data(null)
                        .build();
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            LeaveEntity leaveEntity = optionalLeave.get();

            // Check if the leave is already marked as deleted
            if (leaveEntity.isDelete()) {
                String errorMessage = "The leave with ID " + id + " is already marked as deleted.";
                log.warn(errorMessage);
                ApiResponse<LeaveEntity> response = ApiResponse.<LeaveEntity>builder()
                        .message(errorMessage)
                        .status(HttpStatus.CONFLICT.value())
                        .data(null)
                        .build();
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }

            // Check if the leave status is "approved" or "rejected"
            String status = leaveEntity.getStatus();
            if ("approved".equalsIgnoreCase(status) || "rejected".equalsIgnoreCase(status)) {
                String errorMessage = "Cannot delete the leave with ID " + id + " as it is " + status + ".";
                log.warn(errorMessage);
                ApiResponse<LeaveEntity> response = ApiResponse.<LeaveEntity>builder()
                        .message(errorMessage)
                        .status(HttpStatus.CONFLICT.value())
                        .data(null)
                        .build();
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }

            // Mark the leave entity as deleted and update the deletion timestamp
            leaveEntity.setDelete(true);
            leaveEntity.setDeletedAt(LocalDateTime.now());

            // Save the changes to the repository
            leaverepo.save(leaveEntity);

            ApiResponse<LeaveEntity> response = ApiResponse.<LeaveEntity>builder()
                    .message("Leave successfully marked as deleted")
                    .status(HttpStatus.OK.value())
                    .data(leaveEntity)
                    .build();

            log.info("Successfully marked leave with ID {} as deleted", id);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Exception occurred while deleting leave with ID {}: {}", id, e.getMessage(), e);
            ApiResponse<LeaveEntity> response = ApiResponse.<LeaveEntity>builder()
                    .message("An unexpected error occurred while processing the request.")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .data(null)
                    .build();
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SneakyThrows
    public boolean hasEnoughLeaves(UUID id, int requiredDays) {
        Optional<EmployeeEntity> employee = erepository.findById(id);
        if (employee.isEmpty())
            throw new UserNotFoundException();

        if (employee.get().getLeaveCount() < requiredDays) {
            return false;
        }
        return true;
    }

    public ErrorUtil<String, String> acceptLeave(AcceptLeaveEntity entity) {
        if (!isEmployeeExists(entity.getReviewedBy().getId())) {
            return new ErrorUtil<>(false, "Not a valid reviewer", null);
        }

        var leave = leaverepo.findById(entity.getLeaveRequest().getId());
        if (leave.isEmpty()) {
            return new ErrorUtil<>(false, "Not a valid leave ID", null);
        }

        int requiredDays = (int) leave.get().getStartDate().until(
                leave.get().getEndDate(),
                ChronoUnit.DAYS);

        if (!hasEnoughLeaves(leave.get().getEmployee().getId(), requiredDays)) {
            return new ErrorUtil<>(false, "Employees does not have enough leave", null);
        }

        if (rejectLeaveEntityRepo.findById(entity.getLeaveRequest().getId()).isPresent()) {
            entity.setStatus(LeaveStatus.APPROVED);
        } else {
            entity.setStatus(LeaveStatus.REAPPROVED);
        }
        acceptLeaveEntityRepo.save(entity);

        EmployeeEntity employee = leave.get().getEmployee();
        employee.setLeaveCount(employee.getLeaveCount() - requiredDays);
        erepository.save(employee);
        Optional<LeaveEntity> leaveOptional = Optional.of(leave.get());
        if (leaveOptional.isEmpty() || leaveOptional.get().getAssigningEmail() == null) {
            log.error("Assigning email is null or leave information is missing.");
            throw new IllegalStateException("Cannot send email without a valid assigning email.");
        }

        String assigningEmail = leaveOptional.get().getAssigningEmail();

        // Get the employee email and check for null
        String toEmail = employee.getEmail();
        if (toEmail == null) {
            log.error("Employee email is null.");
            throw new IllegalStateException("Cannot send email without a valid employee email.");
        }

        // Set up the mail sender
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername(assigningEmail);
        mailSender.setPassword("gwsphcbdsbjgolll");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // Ensure STARTTLS is enabled
        props.put("mail.debug", "true");

        // Create and send the email
        SimpleMailMessage acceptedMail = new SimpleMailMessage();
        acceptedMail.setFrom(assigningEmail);
        acceptedMail.setTo(toEmail);
        acceptedMail.setSubject("Leave Application Accepted");
        acceptedMail.setText("Your leave application for " + leaveOptional.get().getLeaveType() + " from "
                + leaveOptional.get().getStartDate() + " to " + leaveOptional.get().getEndDate()
                + " has been accepted.");

        String[] ccEmails = { "cc1@example.com", "cc2@example.com" }; // Replace with actual CC email addresses
        acceptedMail.setCc(ccEmails);

        // Send the email
        try {
            mailSender.send(acceptedMail);
            log.info("Leave acceptance email sent to {}", toEmail);
        } catch (MailException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }

        return new ErrorUtil<>(true, null, "leave accepted succesfully");
    }

    @Override
    public ErrorUtil<String, String> rejectLeave(RejectLeaveEntity entity) {
        if (!isEmployeeExists(entity.getReviewedBy().getId())) {
            log.error("Not a valid reviewer %s", entity.getReviewedBy().getId().toString());
            return new ErrorUtil<>(false, "Not a valid reviewer", null);
        }

        var leave = leaverepo.findById(entity.getLeaveRequest().getId());
        if (leave.isEmpty()) {
            log.error("Not a valid request id %s", entity.getLeaveRequest().getId().toString());
            return new ErrorUtil<>(false, "Not a valid leave ID", null);
        }

        entity.setStatus(LeaveStatus.REJECTED);
        rejectLeaveEntityRepo.save(entity);
        String assigningEmailFrom = leave.get().getAssigningEmail();
        String employeeEmailTo = leave.get().getEmployee().getEmail();

        // Check if email addresses are valid
        if (assigningEmailFrom == null || assigningEmailFrom.isEmpty()) {
            log.error("Assigning email is null or empty.");
            throw new IllegalStateException("Cannot send email without a valid assigning email.");
        }
        if (employeeEmailTo == null || employeeEmailTo.isEmpty()) {
            log.error("Employee email is null or empty.");
            throw new IllegalStateException("Cannot send email without a valid employee email.");
        }

        // Set up the mail sender
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername(assigningEmailFrom);
        mailSender.setPassword("gwsphcbdsbjgolll"); // Ensure secure handling of this password

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // Ensure STARTTLS is enabled
        props.put("mail.debug", "true");

        // Create and configure the email
        SimpleMailMessage rejectedMail = new SimpleMailMessage();
        rejectedMail.setFrom(assigningEmailFrom);
        rejectedMail.setTo(employeeEmailTo);
        rejectedMail.setSubject("Leave Application Rejected");
        rejectedMail.setText("Your leave application for " + leave.get().getLeaveType() + " has been rejected.");

        // Add CC recipients
        String[] ccEmails = { "cc1@example.com", "cc2@example.com" }; // Replace with actual CC email addresses
        rejectedMail.setCc(ccEmails);

        // Send the email
        try {
            mailSender.send(rejectedMail);
            log.info("Leave rejection email sent to {}", employeeEmailTo);
        } catch (MailException e) {
            log.error("Failed to send email to {}: {}", employeeEmailTo, e.getMessage());
        }

        return new ErrorUtil<>(true, null, "leave rejected successfully");
    }
}
