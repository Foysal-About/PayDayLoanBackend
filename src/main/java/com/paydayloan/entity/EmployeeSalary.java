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
@Table(name = "PDL_EMPLOYEE_SALARY")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSalary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SALARY_ID")
    private Long salaryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYEE_ID", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CORPORATE_ID", nullable = false)
    private Corporate corporate;

    @Column(name = "MONTHLY_SALARY", nullable = false, precision = 18, scale = 2)
    private BigDecimal monthlySalary;

    @Column(name = "ELIGIBLE_PERCENT", precision = 5, scale = 2)
    private BigDecimal eligiblePercent = BigDecimal.valueOf(80);

    @Column(name = "MAX_ELIGIBLE_AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal maxEligibleAmount;

    @Column(name = "EFFECTIVE_FROM", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "EFFECTIVE_TO")
    private LocalDate effectiveTo;

    @Column(name = "IS_CURRENT_YN")
    private Integer isCurrentYn = 1;

    @Column(name = "APPROVAL_STATUS")
    private String approvalStatus = "APPROVED";

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
