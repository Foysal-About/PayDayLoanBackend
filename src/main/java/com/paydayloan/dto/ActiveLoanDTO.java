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
public class ActiveLoanDTO {
    private Long loanId;
    private String loanRefNo;
    private Long requestId;
    private Long employeeId;
    private BigDecimal sanctionedAmount;
    private BigDecimal serviceChargeAmount;
    private BigDecimal disbursedAmount;
    private BigDecimal outstandingAmount;
    private LocalDate maturityDate;
    private String loanStatus;
}
