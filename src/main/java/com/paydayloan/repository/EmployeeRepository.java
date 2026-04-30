package com.paydayloan.repository;

import com.paydayloan.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByCorporateCorporateId(Long corporateId);
    Optional<Employee> findByCorporateCorporateIdAndEmployeeCode(Long corporateId, String employeeCode);
    Optional<Employee> findByCorporateCorporateIdAndCustomerId(Long corporateId, Long customerId);
    List<Employee> findByStatus(String status);
}
