package com.example.LeaveManagementSystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private boolean isActive = true;

    @Column(nullable = false)
    private boolean isDelete;

    @OneToMany
    private Set<WorkingHours> hours;

    @Column
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Integer leaveCount;
    private String password;
    private String encryptedPassword;
    private  Integer employeeSalary;
}
