package com.paydayloan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PDL_DISBURSEMENT_TXN")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisbursementTxn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DISBURSEMENT_TXN_ID")
    private Long disbursementTxnId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOAN_ID", nullable = false)
    private LoanAccount loanAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUEST_ID", nullable = false)
    private LoanRequest loanRequest;

    @Column(name = "TXN_REF_NO", nullable = false, unique = true)
    private String txnRefNo;

    @Column(name = "TXN_DATE", nullable = false, updatable = false)
    private LocalDateTime txnDate;

    @Column(name = "SOURCE_ACCOUNT_NO", nullable = false)
    private String sourceAccountNo;

    @Column(name = "DESTINATION_ACCOUNT_NO", nullable = false)
    private String destinationAccountNo;

    @Column(name = "GROSS_AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "SERVICE_CHARGE_AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal serviceChargeAmount;

    @Column(name = "NET_AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "CBS_TXN_REF_NO")
    private String cbsTxnRefNo;

    @Column(name = "TXN_STATUS")
    private String txnStatus = "SUCCESS";

    @Column(name = "FAILURE_REASON")
    private String failureReason;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        txnDate = LocalDateTime.now();
        createdAt = LocalDateTime.now();
    }
}
