package com.example.LeaveManagementSystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class PayslipPdf {

    @Id
    private UUID id;

    @Column(name = "file", columnDefinition = "BYTEA")
    private byte[] file;



}
