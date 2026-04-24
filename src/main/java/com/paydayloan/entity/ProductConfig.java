package com.paydayloan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "PDL_PRODUCT_CONFIG")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRODUCT_CONFIG_ID")
    private Long productConfigId;

    @Column(name = "PRODUCT_CODE", nullable = false, unique = true)
    private String productCode;

    @Column(name = "PRODUCT_NAME", nullable = false)
    private String productName;

    @Column(name = "MAX_ELIGIBLE_PERCENT", precision = 5, scale = 2)
    private BigDecimal maxEligiblePercent = BigDecimal.valueOf(80);

    @Column(name = "SERVICE_CHARGE_PERCENT", precision = 5, scale = 2)
    private BigDecimal serviceChargePercent = BigDecimal.valueOf(2);

    @Column(name = "MIN_SERVICE_CHARGE", precision = 18, scale = 2)
    private BigDecimal minServiceCharge = BigDecimal.valueOf(200);

    @Column(name = "MAX_ACTIVE_LOAN_PER_EMP")
    private Integer maxActiveLoanPerEmp = 1;

    @Column(name = "MIN_REQUEST_AMOUNT", precision = 18, scale = 2)
    private BigDecimal minRequestAmount = BigDecimal.valueOf(500);

    @Column(name = "MAX_REQUEST_AMOUNT", precision = 18, scale = 2)
    private BigDecimal maxRequestAmount;

    @Column(name = "REPAYMENT_DUE_DAYS")
    private Integer repaymentDueDays = 30;

    @Column(name = "EMPLOYER_APPROVAL_REQUIRED_YN")
    private Integer employerApprovalRequiredYn = 1;

    @Column(name = "AUTO_DISBURSE_YN")
    private Integer autoDisbursYn = 1;

    @Column(name = "STATUS")
    private String status = "ACTIVE";

    @Column(name = "EFFECTIVE_FROM", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "EFFECTIVE_TO")
    private LocalDate effectiveTo;

    @Column(name = "CREATED_BY", nullable = false)
    private String createdBy;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
