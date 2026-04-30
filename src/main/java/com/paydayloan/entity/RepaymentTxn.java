package com.paydayloan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PDL_REPAYMENT_TXN")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepaymentTxn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REPAYMENT_TXN_ID")
    private Long repaymentTxnId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOAN_ID", nullable = false)
    private LoanAccount loanAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCHEDULE_ID")
    private RepaymentSchedule repaymentSchedule;

    @Column(name = "TXN_REF_NO", nullable = false, unique = true)
    private String txnRefNo;

    @Column(name = "TXN_DATE", nullable = false, updatable = false)
    private LocalDateTime txnDate;

    @Column(name = "TXN_SOURCE", nullable = false)
    private String txnSource;

    @Column(name = "DEBIT_ACCOUNT_NO")
    private String debitAccountNo;

    @Column(name = "CREDIT_ACCOUNT_NO")
    private String creditAccountNo;

    @Builder.Default
    @Column(name = "PRINCIPAL_PAID", precision = 18, scale = 2)
    private BigDecimal principalPaid = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "CHARGE_PAID", precision = 18, scale = 2)
    private BigDecimal chargePaid = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "PENALTY_PAID", precision = 18, scale = 2)
    private BigDecimal penaltyPaid = BigDecimal.ZERO;

    @Column(name = "TOTAL_PAID", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalPaid;

    @Column(name = "CBS_TXN_REF_NO")
    private String cbsTxnRefNo;

    @Builder.Default
    @Column(name = "TXN_STATUS")
    private String txnStatus = "SUCCESS";

    @Column(name = "REMARKS")
    private String remarks;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        txnDate = LocalDateTime.now();
        createdAt = LocalDateTime.now();
    }
}
