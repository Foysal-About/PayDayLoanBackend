package com.paydayloan.repository;

import com.paydayloan.entity.ProductConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductConfigRepository extends JpaRepository<ProductConfig, Long> {
    Optional<ProductConfig> findByProductCode(String productCode);
    List<ProductConfig> findByStatus(String status);
}
