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
@Table(name = "PDL_EMPLOYEE_LIMIT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EMPLOYEE_LIMIT_ID")
    private Long employeeLimitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYEE_ID", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CORPORATE_ID", nullable = false)
    private Corporate corporate;

    @Column(name = "MONTHLY_SALARY", nullable = false, precision = 18, scale = 2)
    private BigDecimal monthlySalary;

    @Column(name = "ELIGIBLE_PERCENT", nullable = false, precision = 5, scale = 2)
    private BigDecimal eligiblePercent;

    @Column(name = "MAX_ELIGIBLE_AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal maxEligibleAmount;

    @Builder.Default
    @Column(name = "UTILIZED_AMOUNT", precision = 18, scale = 2)
    private BigDecimal utilizedAmount = BigDecimal.ZERO;

    @Column(name = "AVAILABLE_AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal availableAmount;

    @Builder.Default
    @Column(name = "ACTIVE_LOAN_COUNT")
    private Integer activeLoanCount = 0;

    @Column(name = "AS_OF_DATE", nullable = false)
    private LocalDate asOfDate;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

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
