package com.paydayloan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "PDL_EMPLOYEE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EMPLOYEE_ID")
    private Long employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CORPORATE_ID", nullable = false)
    private Corporate corporate;

    @Column(name = "EMPLOYEE_CODE", nullable = false)
    private String employeeCode;

    @Column(name = "EMPLOYEE_NAME", nullable = false)
    private String employeeName;

    @Column(name = "CUSTOMER_ID", nullable = false)
    private Long customerId;

    @Column(name = "CIF_ID")
    private Long cifId;

    @Column(name = "MOBILE_NO", nullable = false)
    private String mobileNo;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "NID_NO")
    private String nidNo;

    @Column(name = "DESIGNATION")
    private String designation;

    @Column(name = "DEPARTMENT")
    private String department;

    @Column(name = "JOIN_DATE")
    private LocalDate joinDate;

    @Column(name = "EMPLOYMENT_STATUS")
    private String employmentStatus = "ACTIVE";

    @Column(name = "SALARY_ACCOUNT_NO", nullable = false)
    private String salaryAccountNo;

    @Column(name = "REPAYMENT_ACCOUNT_NO")
    private String repaymentAccountNo;

    @Column(name = "ELIGIBILITY_YN")
    private Integer eligibilityYn = 1;

    @Column(name = "STATUS")
    private String status = "ACTIVE";

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
