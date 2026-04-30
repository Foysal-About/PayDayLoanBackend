package com.paydayloan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "PDL_REQUEST_APPROVAL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "APPROVAL_ID")
    private Long approvalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUEST_ID", nullable = false)
    private LoanRequest loanRequest;

    @Column(name = "APPROVAL_LEVEL", nullable = false)
    private Integer approvalLevel;

    @Column(name = "APPROVER_TYPE", nullable = false)
    private String approverType;

    @Column(name = "APPROVER_USER_ID", nullable = false)
    private String approverUserId;

    @Column(name = "APPROVER_NAME")
    private String approverName;

    @Builder.Default
    @Column(name = "APPROVAL_STATUS")
    private String approvalStatus = "PENDING";

    @Column(name = "APPROVAL_DATE")
    private LocalDateTime approvalDate;

    @Column(name = "APPROVAL_REMARKS")
    private String approvalRemarks;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
