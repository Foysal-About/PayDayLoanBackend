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
public class EmployeeSalaryDTO {
    private Long salaryId;
    private Long employeeId;
    private Long corporateId;
    private BigDecimal monthlySalary;
    private BigDecimal eligiblePercent;
    private BigDecimal maxEligibleAmount;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Integer isCurrentYn;
    private String approvalStatus;
    private String remarks;
}
