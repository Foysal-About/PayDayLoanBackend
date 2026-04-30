package com.paydayloan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "PDL_CORPORATE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Corporate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CORPORATE_ID")
    private Long corporateId;

    @Column(name = "CORPORATE_CODE", nullable = false, unique = true)
    private String corporateCode;

    @Column(name = "CORPORATE_NAME", nullable = false)
    private String corporateName;

    @Column(name = "SHORT_NAME")
    private String shortName;

    @Column(name = "CUSTOMER_ID")
    private Long customerId;

    @Builder.Default
    @Column(name = "AGREEMENT_STATUS")
    private String agreementStatus = "PENDING";

    @Column(name = "AGREEMENT_DATE")
    private LocalDate agreementDate;

    @Column(name = "EFFECTIVE_FROM")
    private LocalDate effectiveFrom;

    @Column(name = "EFFECTIVE_TO")
    private LocalDate effectiveTo;

    @Builder.Default
    @Column(name = "GUARANTEE_MODE")
    private String guaranteeMode = "CORPORATE_GUARANTEE";

    @Builder.Default
    @Column(name = "MAX_ELIGIBLE_PERCENT", precision = 5, scale = 2)
    private BigDecimal maxEligiblePercent = BigDecimal.valueOf(80);

    @Builder.Default
    @Column(name = "SERVICE_CHARGE_PERCENT", precision = 5, scale = 2)
    private BigDecimal serviceChargePercent = BigDecimal.valueOf(2);

    @Builder.Default
    @Column(name = "MIN_SERVICE_CHARGE", precision = 18, scale = 2)
    private BigDecimal minServiceCharge = BigDecimal.valueOf(200);

    @Builder.Default
    @Column(name = "MAX_ACTIVE_LOAN_PER_EMP")
    private Integer maxActiveLoanPerEmp = 1;

    @Builder.Default
    @Column(name = "REPAYMENT_MODE")
    private String repaymentMode = "SALARY_DEDUCTION";

    @Builder.Default
    @Column(name = "AUTO_DISBURSE_YN")
    private Integer autoDisbursYn = 1;

    @Builder.Default
    @Column(name = "STATUS")
    private String status = "ACTIVE";

    @Column(name = "REMARKS")
    private String remarks;

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
