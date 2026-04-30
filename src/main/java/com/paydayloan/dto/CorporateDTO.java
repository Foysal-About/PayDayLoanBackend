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
public class CorporateDTO {
    private Long corporateId;
    private String corporateCode;
    private String corporateName;
    private String shortName;
    private Long customerId;
    private String agreementStatus;
    private LocalDate agreementDate;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String guaranteeMode;
    private BigDecimal maxEligiblePercent;
    private BigDecimal serviceChargePercent;
    private BigDecimal minServiceCharge;
    private Integer maxActiveLoanPerEmp;
    private String repaymentMode;
    private Integer autoDisbursYn;
    private String status;
    private String remarks;
}
