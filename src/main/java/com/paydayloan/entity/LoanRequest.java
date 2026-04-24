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
@Table(name = "PDL_LOAN_REQUEST")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REQUEST_ID")
    private Long requestId;

    @Column(name = "REQUEST_REF_NO", nullable = false, unique = true)
    private String requestRefNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_CONFIG_ID", nullable = false)
    private ProductConfig productConfig;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CORPORATE_ID", nullable = false)
    private Corporate corporate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYEE_ID", nullable = false)
    private Employee employee;

    @Column(name = "CUSTOMER_ID", nullable = false)
    private Long customerId;

    @Column(name = "REQUEST_CHANNEL")
    private String requestChannel = "MOBILE_APP";

    @Column(name = "REQUEST_DATE", nullable = false, updatable = false)
    private LocalDateTime requestDate;

    @Column(name = "MONTHLY_SALARY", nullable = false, precision = 18, scale = 2)
    private BigDecimal monthlySalary;

    @Column(name = "ELIGIBLE_PERCENT", nullable = false, precision = 5, scale = 2)
    private BigDecimal eligiblePercent;

    @Column(name = "ELIGIBLE_AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal eligibleAmount;

    @Column(name = "REQUESTED_AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "SERVICE_CHARGE_AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal serviceChargeAmount;

    @Column(name = "NET_DISBURSE_AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal netDisburseAmount;

    @Column(name = "PURPOSE")
    private String purpose;

    @Column(name = "REPAYMENT_DATE", nullable = false)
    private LocalDate repaymentDate;

    @Column(name = "REPAYMENT_SOURCE")
    private String repaymentSource = "SALARY_ACCOUNT";

    @Column(name = "REQUEST_STATUS")
    private String requestStatus = "PENDING_CORP_APPROVAL";

    @Column(name = "STATUS_REMARKS")
    private String statusRemarks;

    @Column(name = "OTP_VERIFIED_YN")
    private Integer otpVerifiedYn = 0;

    @Column(name = "TXN_PIN_VERIFIED_YN")
    private Integer txnPinVerifiedYn = 0;

    @Column(name = "CANCELLED_BY")
    private String cancelledBy;

    @Column(name = "CANCELLED_AT")
    private LocalDateTime cancelledAt;

    @Column(name = "CREATED_BY", nullable = false)
    private String createdBy;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "loanRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RequestApproval> approvals;

    @OneToOne(mappedBy = "loanRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private LoanAccount loanAccount;

    @PrePersist
    protected void onCreate() {
        requestDate = LocalDateTime.now();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
