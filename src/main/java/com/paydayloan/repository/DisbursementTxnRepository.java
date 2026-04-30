package com.paydayloan.repository;

import com.paydayloan.entity.DisbursementTxn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisbursementTxnRepository extends JpaRepository<DisbursementTxn, Long> {
}
