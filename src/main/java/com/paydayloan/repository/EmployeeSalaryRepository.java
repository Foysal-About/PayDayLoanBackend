package com.paydayloan.repository;

import com.paydayloan.entity.EmployeeSalary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeSalaryRepository extends JpaRepository<EmployeeSalary, Long> {
    Optional<EmployeeSalary> findByEmployeeEmployeeIdAndIsCurrentYn(Long employeeId, Integer isCurrentYn);
}
