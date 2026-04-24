package com.paydayloan.repository;

import com.paydayloan.entity.CorporateUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CorporateUserRepository extends JpaRepository<CorporateUser, Long> {
    Optional<CorporateUser> findByUserLoginId(String userLoginId);
    List<CorporateUser> findByCorporateCorporateId(Long corporateId);
    List<CorporateUser> findByStatus(String status);
    Optional<CorporateUser> findByEmail(String email);
}
