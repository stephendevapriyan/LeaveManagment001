package com.example.LeaveManagementSystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "leave_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = true)
    private EmployeeEntity employee;

    @Column
    @DateTimeFormat(pattern = "yyyy-mm-dd")
    private LocalDate startDate;

    @Column
    @DateTimeFormat(pattern = "yyyy-mm-dd")
    private LocalDate endDate;

    @Column(nullable = false)
    private String leaveType;

    @Column(nullable = false)
    private String status;

    @Column
    private LocalDateTime requestDate;

    @Column(nullable = false)
    private String leaveReason;

    @Column
    private boolean isDelete;

    @OneToMany
    private List<AcceptLeaveEntity> acceptedLeaves;

    @Column
    private LocalDateTime approvedDate;
    @Column
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @Column
    private LocalDateTime deletedAt;
    @Column(nullable = false)
    private  String assigningEmail;




}


