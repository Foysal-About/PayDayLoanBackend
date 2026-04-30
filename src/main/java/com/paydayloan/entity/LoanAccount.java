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
@Table(name = "PDL_LOAN_ACCOUNT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LOAN_ID")
    private Long loanId;

    @Column(name = "LOAN_REF_NO", nullable = false, unique = true)
    private String loanRefNo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUEST_ID", nullable = false, unique = true)
    private LoanRequest loanRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CORPORATE_ID", nullable = false)
    private Corporate corporate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYEE_ID", nullable = false)
    private Employee employee;

    @Column(name = "CUSTOMER_ID", nullable = false)
    private Long customerId;

    @Column(name = "LOAN_ACCOUNT_NO")
    private String loanAccountNo;

    @Column(name = "DISBURSEMENT_ACCOUNT_NO", nullable = false)
    private String disbursementAccountNo;

    @Column(name = "REPAYMENT_ACCOUNT_NO", nullable = false)
    private String repaymentAccountNo;

    @Column(name = "SANCTIONED_AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal sanctionedAmount;

    @Column(name = "SERVICE_CHARGE_AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal serviceChargeAmount;

    @Column(name = "DISBURSED_AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal disbursedAmount;

    @Column(name = "OUTSTANDING_AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal outstandingAmount;

    @Column(name = "DISBURSEMENT_DATE")
    private LocalDateTime disbursementDate;

    @Column(name = "VALUE_DATE")
    private LocalDate valueDate;

    @Column(name = "MATURITY_DATE", nullable = false)
    private LocalDate maturityDate;

    @Builder.Default
    @Column(name = "LOAN_STATUS")
    private String loanStatus = "ACTIVE";

    @Column(name = "CLOSED_DATE")
    private LocalDate closedDate;

    @Column(name = "CBS_TXN_REF_NO")
    private String cbsTxnRefNo;

    @Column(name = "CREATED_BY", nullable = false)
    private String createdBy;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "loanAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LoanCharge> loanCharges;

    @OneToMany(mappedBy = "loanAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RepaymentSchedule> repaymentSchedules;

    @OneToMany(mappedBy = "loanAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DisbursementTxn> disbursementTxns;

    @OneToMany(mappedBy = "loanAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
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
