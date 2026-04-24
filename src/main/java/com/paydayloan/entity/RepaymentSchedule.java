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
@Table(name = "PDL_REPAYMENT_SCHEDULE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepaymentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SCHEDULE_ID")
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOAN_ID", nullable = false)
    private LoanAccount loanAccount;

    @Column(name = "INSTALLMENT_NO", nullable = false)
    private Integer installmentNo;

    @Column(name = "DUE_DATE", nullable = false)
    private LocalDate dueDate;

    @Column(name = "PRINCIPAL_DUE", nullable = false, precision = 18, scale = 2)
    private BigDecimal principalDue;

    @Column(name = "CHARGE_DUE", nullable = false, precision = 18, scale = 2)
    private BigDecimal chargeDue;

    @Column(name = "TOTAL_DUE", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalDue;

    @Column(name = "PAID_AMOUNT", precision = 18, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "OUTSTANDING_DUE", nullable = false, precision = 18, scale = 2)
    private BigDecimal outstandingDue;

    @Column(name = "SCHEDULE_STATUS")
    private String scheduleStatus = "PENDING";

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "repaymentSchedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RepaymentTxn> repaymentTxns;

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
