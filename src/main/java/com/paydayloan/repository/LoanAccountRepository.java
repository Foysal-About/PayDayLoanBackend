package com.paydayloan.repository;

import com.paydayloan.entity.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanAccountRepository extends JpaRepository<LoanAccount, Long> {
    List<LoanAccount> findByEmployeeEmployeeIdAndLoanStatus(Long employeeId, String loanStatus);
}
