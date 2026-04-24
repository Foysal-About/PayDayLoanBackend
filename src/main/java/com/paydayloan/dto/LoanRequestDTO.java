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
public class LoanRequestDTO {
    private Long requestId;
    private String requestRefNo;
    private Long employeeId;
    private Long customerId;
    private Long corporateId;
    private Long productConfigId;
    private BigDecimal requestedAmount;
    private LocalDate repaymentDate;
    private String purpose;
    private String requestChannel;
    private String requestStatus;
    private BigDecimal eligibleAmount;
    private BigDecimal serviceChargeAmount;
    private BigDecimal netDisburseAmount;
}

