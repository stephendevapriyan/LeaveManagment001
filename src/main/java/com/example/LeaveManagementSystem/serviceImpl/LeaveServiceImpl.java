package com.example.LeaveManagementSystem.serviceImpl;

import com.example.LeaveManagementSystem.dto.EmployeeResponseDTO;
import com.example.LeaveManagementSystem.dto.LeaveResponseDTO;
import com.example.LeaveManagementSystem.entity.AcceptLeaveEntity;
import com.example.LeaveManagementSystem.entity.EmployeeEntity;
import com.example.LeaveManagementSystem.entity.LeaveEntity;
import com.example.LeaveManagementSystem.entity.LeaveStatus;
import com.example.LeaveManagementSystem.entity.OrganizationEntity;
import com.example.LeaveManagementSystem.entity.RejectLeaveEntity;
import com.example.LeaveManagementSystem.exceptions.IdNotFoundException;
import com.example.LeaveManagementSystem.exceptions.UserNotFoundException;
import com.example.LeaveManagementSystem.exceptions.ValidationException;
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
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;
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
    public ApiResponse<OrganizationEntity> saveOrganization(OrganizationEntity oentity, boolean isUpdate) {
        validateOrganization(oentity);
        log.info("save organization method started");
        try {
            Optional<OrganizationEntity> orgOpt = orepo.findByEmail(oentity.getEmail());
            if (orgOpt.isPresent() && orgOpt.get().isDelete() && !isUpdate) {
                return ApiResponse.<OrganizationEntity>builder()
                        .message("Organization has been deleted earlier please update it")
                        .status(HttpStatus.OK.value())
                        .data(null)
                        .build();
            }
            if (!isUpdate && (organizationEmailExists(oentity.getEmail()) || checkLocation(oentity.getLocation()))) {
                log.warn("already registered company with give email or location");
               throw new ValidationException("Company is already registered company with give email or location");
            }
            if (!emailValidation.isEmailValid(oentity.getEmail())) {
                log.warn("invalid email id ");
                throw new ValidationException("Invalid Email Id");
            }
            if (!mobileNoValidation.isNumberValid(oentity.getContactNumber())) {
                log.warn("invalid mobile no");
               throw new ValidationException("Invalid mobile number");
            }
            if (isUpdate && orgOpt.isEmpty()) {
                log.warn("trying to update organization that is not present");
                throw new ValidationException("No organization found to update");
            }

            String status;
            OrganizationEntity savedEntity;
            if (isUpdate) {
                OrganizationEntity org = orgOpt.get();
                org.setName(oentity.getName());
                org.setAddress(oentity.getAddress());
                org.setLocation(oentity.getLocation());
                org.setEmail(oentity.getEmail());
                org.setContactNumber(oentity.getContactNumber());
                org.setUpdatedAt(LocalDateTime.now());
                org.setDelete(oentity.isDelete());
                org.setActive(oentity.isActive());
                if (oentity.isDelete()) {
                    org.setDeletedAt(LocalDateTime.now());
                }
                savedEntity = orepo.save(org);
                status = "updated";
            } else {
                status = "saved";
                oentity.setActive(true);
                savedEntity = orepo.save(oentity);
            }

            log.info(String.format("successfully %s organization", status));
            return ApiResponse.<OrganizationEntity>builder()
                    .message(String.format("successfully %s organization", status))
                    .status(HttpStatus.OK.value())
                    .data(savedEntity)
                    .build();
        }
        catch(ValidationException e){
            throw e;
        }
        catch (Exception e) {
            log.error("Unexpected error occurred: " + e.getMessage());
            throw new RuntimeException("Unexpected error occurred: " + e.getMessage());
        }
        finally {
            log.info("save organization method completed");
        }
    }

    public void validateOrganization(OrganizationEntity organization){
        if(organization.getName()==null || organization.getName().isBlank()){
            throw new ValidationException("Name is Mandatory");
        }
        if(organization.getAddress()==null || organization.getAddress().isBlank()){
            throw new ValidationException("Address is Mandatory");
        }
        if(organization.getLocation()==null || organization.getLocation().isBlank()){
            throw new ValidationException("Location is Mandatory");
        }
        if(organization.getEmail()==null || organization.getEmail().isBlank()){
            throw new ValidationException("Email Id is Mandatory");
        }
        if(organization.getContactNumber()==null || organization.getContactNumber().isBlank()){
            throw new ValidationException("Contact number is Mandatory");
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
    public ApiResponse<EmployeeResponseDTO> saveEmployee(EmployeeEntity entity, boolean isUpdate) {
        validateEmployee(entity);
        Set<String> validRoles = Set.of(
                "HR", "DEVELOPER", "SOFTWARE_TESTING_ENGINEER",
                "PROJECT_MANAGER", "BUSINESS_ANALYST", "TECHNICAL_ARCHITECTURE"
        );
        String inputRole=entity.getRole().trim().toUpperCase().replaceAll("\\s+","_");

        log.info("saving employee method started");
        try {
            if (!validRoles.contains(inputRole)){
                throw new ValidationException("Invalid Role");
            }
            if (!isUpdate && isEmailExists(entity.getEmail())) {
                log.warn("Email id already exists");
               throw new ValidationException("Employee Email id is already exists");
            }
            if (entity.getOrganization() == null || !isOrganizationExists(entity.getOrganization().getId())) {
                log.warn("Organization Id not found");
                throw new IdNotFoundException("Organization Id not found");
            }
            if (!emailValidation.isEmailValid(entity.getEmail())) {
                log.warn("invalid email id ");
               throw new ValidationException("Invalid Email Id ");
            }
            if (!mobileNoValidation.isNumberValid(entity.getPhoneNumber())) {
                log.warn("invalid mobile no ");
               throw new ValidationException("Invalid Mobile number");
            }

            EmployeeEntity savedEntity;
            String status;
            if (isUpdate) {
                log.info("updating the user");
                OrganizationEntity org = orepo.findById(entity.getOrganization().getId()).get();
                savedEntity = erepository.findByEmail(entity.getEmail()).get();
                savedEntity.setFirstname(entity.getFirstname());
                savedEntity.setLastname(entity.getLastname());
                savedEntity.setEmail(entity.getEmail());
                savedEntity.setRole(entity.getRole());
                savedEntity.setPhoneNumber(entity.getPhoneNumber());
                savedEntity.setHireDate(entity.getHireDate());
                savedEntity.setJobTitle(entity.getJobTitle());
                savedEntity.setOrganization(org);
                savedEntity.setActive(entity.isActive());
                savedEntity.setCreatedAt(entity.getCreatedAt());
                savedEntity.setUpdatedAt(entity.getUpdatedAt());
                savedEntity.setRole(inputRole);
                savedEntity.setAvailableLeaves(entity.getLeaveCount());
                savedEntity.setActive(entity.isActive());
                savedEntity.setDayShift(entity.isDayShift());
                savedEntity.setNightShift(entity.isNightShift());
                status = " updated";
            } else {
                log.info("creating the user");
                savedEntity = entity;
                status = "saved";
            }
            entity.setAvailableLeaves(entity.getLeaveCount());
            entity.setActive(true);
            erepository.save(savedEntity);
            log.info("Successfully saved employee");

            EmployeeResponseDTO dto = new EmployeeResponseDTO();

            dto.setId(savedEntity.getId());
            dto.setFirstname(savedEntity.getFirstname());
            dto.setLastname(savedEntity.getLastname());
            dto.setEmail(savedEntity.getEmail());
            dto.setRole(savedEntity.getRole());
            dto.setPhoneNumber(savedEntity.getPhoneNumber());
            dto.setHireDate(savedEntity.getHireDate());
            dto.setJobTitle(savedEntity.getJobTitle());
            dto.setOrganizationId(savedEntity.getOrganization().getId());
            dto.setActive(savedEntity.isActive());
            dto.setCreatedAt(savedEntity.getCreatedAt());
            dto.setUpdatedAt(savedEntity.getUpdatedAt());

            return ApiResponse.<EmployeeResponseDTO>builder()
                    .status(HttpStatus.OK.value())
                    .message("Successfully saved Employee Details")
                    .data(dto)
                    .build();
        }
        catch(ValidationException e){
            throw e;
        }
        catch (Exception e) {
            log.error("Unexpected error occurred: " + e.getMessage());
            throw new RuntimeException("Unexpected error occurred: " + e.getMessage());
        }
        finally {
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

    public void validateEmployee(EmployeeEntity employee) {
        if (employee.getFirstname() == null || employee.getFirstname().isBlank()) {
            throw new ValidationException("First name is mandatory.");
        }
        if (employee.getLastname() == null || employee.getLastname().isBlank()) {
            throw new ValidationException("Last name is mandatory.");
        }

        if (employee.getPhoneNumber() == null || employee.getPhoneNumber().isBlank()) {
            throw new ValidationException("Phone number is mandatory.");
        }
        if (employee.getHireDate() == null) {
            throw new ValidationException("Hire date is mandatory.");
        }
        if (employee.getJobTitle() == null || employee.getJobTitle().isBlank()) {
            throw new ValidationException("Job title is mandatory.");
        }

        if (employee.getLeaveCount() == null || employee.getLeaveCount()<0) {
            throw new ValidationException("Leave count is mandatory and should be a positive number.");
        }
        if (employee.getEmployeeSalary() == null || employee.getLeaveCount()<0) {
            throw new ValidationException("Employee salary is mandatory and should be a positive number.");
        }
        if (employee.getDob() == null) {
            throw new ValidationException("Date of birth is mandatory.");
        }
    }


    // apply leave
    @Transactional
    @Override
    public ApiResponse<LeaveResponseDTO> applyLeave(LeaveEntity entity) {
        validateLeave(entity);
        log.info("apply leave method started");
        UUID employeeid = entity.getEmployee().getId();
        System.out.println(employeeid);
       EmployeeEntity employeeEntity = erepository.findById(employeeid).orElseThrow(()-> new IdNotFoundException("Employee Id Not Found"));


        String employeeEmail = employeeEntity.getEmail();
        String assigningEmail = entity.getAssigningEmail();

        Period difference = Period.between(entity.getStartDate(), entity.getEndDate());
        int noOfDays = difference.getDays() + 1;
        try {
            if(! isEmployeeExists(employeeid)){
                throw new ValidationException("Invalid Employee iD");
            }


            if (employeeEntity.getAvailableLeaves() < noOfDays) {
                log.warn("Insufficient leave balance");
                throw new ValidationException("Insufficient leave balance");
            }
            List<LeaveEntity> matchedDates = leaverepo.findLeavesByEmployeeAndDates(employeeid, entity.getStartDate(),
                    entity.getEndDate());
            if (!matchedDates.isEmpty() && entity.getId()==null) {
                log.warn("Leave dates overlap with existing leave records");
               throw new ValidationException("Leave dates overlap with existing leave records");
            }
            if(employeeEntity.isDelete()){
                log.warn("Employee is already deleted and not eligible to apply leave");
                throw new ValidationException("Employee is already deleted and not eligible to apply leave");
            }
            if (entity.getId() != null && leaverepo.existsById(entity.getId())) {
                // It's an update
                LeaveEntity existingLeave = leaverepo.findById(entity.getId()).get();
                Period existingdifference = Period.between(existingLeave.getStartDate(), existingLeave.getEndDate());
                int existingLeaveCount=existingdifference.getDays()+1;
                if(noOfDays>existingLeaveCount){
                    int diff=noOfDays-existingLeaveCount;
                    int totalLeave=employeeEntity.getAvailableLeaves()-diff;
                    employeeEntity.setAvailableLeaves(totalLeave);
                }
                if(noOfDays<existingLeaveCount){
                    int diff=existingLeaveCount-noOfDays;
                    int totalLeave=employeeEntity.getAvailableLeaves()+diff;
                    employeeEntity.setAvailableLeaves(totalLeave);
                }
                existingLeave.setStartDate(entity.getStartDate());
                existingLeave.setEndDate(entity.getEndDate());
                existingLeave.setLeaveType(entity.getLeaveType());
                existingLeave.setLeaveReason(entity.getLeaveReason());
                existingLeave.setStatus(entity.getStatus());
                existingLeave.setUpdatedAt(LocalDateTime.now()); // Assuming you update the timestamp
                LeaveEntity updatedLeave = leaverepo.save(existingLeave);

                // Prepare response DTO
                LeaveResponseDTO updatedDto = createLeaveResponseDTO(updatedLeave);
                return ApiResponse.<LeaveResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Successfully updated leave")
                        .data(updatedDto)
                        .build();
            }

            int leaveBalance=employeeEntity.getAvailableLeaves()-noOfDays;
            employeeEntity.setAvailableLeaves(leaveBalance);
            entity.setStatus("Pending");
            LeaveEntity saved = leaverepo.save(entity);
            log.info("Successfully applied leave");

            // Set up the mail sender
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost("smtp.gmail.com");
            mailSender.setPort(587);
            mailSender.setUsername(employeeEmail);
            mailSender.setPassword("gwsphcbdsbjgolll");

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true"); // Ensure STARTTLS is enabled
            props.put("mail.debug", "true");


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
                throw new ValidationException("Apply leave failed.May be invalid assignee or employee mail ");
            }


            LeaveResponseDTO savedDto = createLeaveResponseDTO(saved);

            return ApiResponse.<LeaveResponseDTO>builder()
                    .status(HttpStatus.OK.value())
                    .message("Successfully applied leave and your leave balance is : "+leaveBalance)
                    .data(savedDto)
                    .build();
        }
        catch (ValidationException e) {
            log.error("Validation failed: {}", e.getMessage());
           throw e;
        }
        catch (Exception e) {
            log.error("Exception occurred: ", e);
            throw new RuntimeException( e.getMessage());
        }
        finally {
            log.info("Apply leave method completed");
        }
    }

    private LeaveResponseDTO createLeaveResponseDTO(LeaveEntity saved) {
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
            return dto;
    }
    @Override
    public boolean isEmployeeExists(UUID id) {
        System.out.println(id);
        log.info("Employee exists method started");
        if (erepository.findById(id).isPresent()) {
           return true;
        }
        return false;
    }

   public void validateLeave(LeaveEntity entity){
       if (entity.getEmployee().getId() == null) {
           throw new ValidationException("Employee ID not Found");
       }
       if (entity.getStartDate() == null) {
           throw new ValidationException("Start date is Mandatory");
       }
       if (entity.getEndDate() == null) {
           throw new ValidationException("End date is Mandatory");
       }
       if (entity.getLeaveType() == null || entity.getLeaveType().isBlank()) {
           throw new ValidationException("Leave type is Mandatory");
       }
       if (entity.getLeaveReason() == null || entity.getLeaveReason().isBlank()) {
           throw new ValidationException("Leave reason is Mandatory");
       }
       if (entity.getAssigningEmail() == null || entity.getAssigningEmail().isBlank()) {
           throw new ValidationException("Assigning email cannot be null or blank");
       }
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
            organizationEntity.setActive(false);
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
            employeeEntity.setActive(false);

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
