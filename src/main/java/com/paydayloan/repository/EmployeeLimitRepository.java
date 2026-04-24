package com.paydayloan.repository;

import com.paydayloan.entity.EmployeeLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeLimitRepository extends JpaRepository<EmployeeLimit, Long> {
}
