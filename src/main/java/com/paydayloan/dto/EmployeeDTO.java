package com.paydayloan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    private Long employeeId;
    private Long corporateId;
    private String employeeCode;
    private String employeeName;
    private Long customerId;
    private Long cifId;
    private String mobileNo;
    private String email;
    private String nidNo;
    private String designation;
    private String department;
    private LocalDate joinDate;
    private String employmentStatus;
    private String salaryAccountNo;
    private String repaymentAccountNo;
    private Integer eligibilityYn;
    private String status;
}
