package com.paydayloan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PDL_LOAN_CHARGE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHARGE_ID")
    private Long chargeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOAN_ID", nullable = false)
    private LoanAccount loanAccount;

    @Column(name = "CHARGE_TYPE", nullable = false)
    private String chargeType;

    @Column(name = "CHARGE_AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal chargeAmount;

    @Column(name = "WAIVED_AMOUNT", precision = 18, scale = 2)
    private BigDecimal waivedAmount = BigDecimal.ZERO;

    @Column(name = "COLLECTED_AMOUNT", precision = 18, scale = 2)
    private BigDecimal collectedAmount = BigDecimal.ZERO;

    @Column(name = "CHARGE_STATUS")
    private String chargeStatus = "PENDING";

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
