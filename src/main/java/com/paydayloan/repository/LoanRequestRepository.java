package com.paydayloan.repository;

import com.paydayloan.entity.LoanRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRequestRepository extends JpaRepository<LoanRequest, Long> {
    List<LoanRequest> findByCorporateCorporateIdAndRequestStatus(Long corporateId, String requestStatus);
}