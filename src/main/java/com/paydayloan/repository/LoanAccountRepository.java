package com.paydayloan.repository;

import com.paydayloan.entity.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanAccountRepository extends JpaRepository<LoanAccount, Long> {
    List<LoanAccount> findByEmployeeEmployeeIdAndLoanStatus(Long employeeId, String loanStatus);
    List<LoanAccount> findByCorporateCorporateId(Long corporateId);
    List<LoanAccount> findByCorporateCorporateIdAndLoanStatus(Long corporateId, String loanStatus);
    List<LoanAccount> findByLoanStatus(String loanStatus);
    Optional<LoanAccount> findByLoanRequestRequestId(Long requestId);
}
