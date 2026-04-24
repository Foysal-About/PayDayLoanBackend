package com.paydayloan.repository;

import com.paydayloan.entity.Corporate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CorporateRepository extends JpaRepository<Corporate, Long> {
    Optional<Corporate> findByCorporateCode(String corporateCode);
    List<Corporate> findByStatus(String status);
    List<Corporate> findByAgreementStatus(String agreementStatus);
}
