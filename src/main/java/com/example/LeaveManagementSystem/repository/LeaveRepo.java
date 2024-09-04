package com.example.LeaveManagementSystem.repository;

import com.example.LeaveManagementSystem.entity.LeaveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRepo extends JpaRepository<LeaveEntity, UUID> {

    @Query(value = "SELECT * FROM leave " +
            "WHERE employee_id = :employeeId " +
            "AND start_date <= :endDate " +
            "AND end_date >= :startDate", nativeQuery = true)
    List<LeaveEntity> findLeavesByEmployeeAndDates(UUID employeeId, LocalDate startDate, LocalDate endDate);

}
