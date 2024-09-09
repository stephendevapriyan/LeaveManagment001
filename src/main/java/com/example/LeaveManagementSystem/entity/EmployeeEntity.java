package com.example.LeaveManagementSystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.Set;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String firstname;

    @Column(nullable = false)
    private String lastname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String phoneNumber;

    @Column
    private Date hireDate;

    @Column(nullable = false)
    private String jobTitle;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "organization_id", nullable = true)
    private OrganizationEntity organization;

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    private boolean isDelete;

    @Column
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @Column
    private LocalDateTime deletedAt;
    private Integer leaveCount;
    private String password;
    private String encryptedPassword;
    private  Double employeeSalary;
    private Integer availableLeaves;
    private Date dob;
    private boolean isNightShift;
    private boolean isDayShift;

}
