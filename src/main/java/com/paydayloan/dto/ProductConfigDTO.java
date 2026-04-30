package com.paydayloan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductConfigDTO {
    private Long productConfigId;
    private String productCode;
    private String productName;
    private BigDecimal maxEligiblePercent;
    private BigDecimal serviceChargePercent;
    private BigDecimal minServiceCharge;
    private Integer maxActiveLoanPerEmp;
    private BigDecimal minRequestAmount;
    private BigDecimal maxRequestAmount;
    private Integer repaymentDueDays;
    private Integer employerApprovalRequiredYn;
    private Integer autoDisbursYn;
    private String status;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}
