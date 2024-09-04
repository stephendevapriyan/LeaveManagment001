package com.example.LeaveManagementSystem.repository;

import com.example.LeaveManagementSystem.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepo extends JpaRepository<OrganizationEntity, UUID> {

    Optional<OrganizationEntity> findByEmail(String name);
    Optional<OrganizationEntity> findByLocation(String location);
}
