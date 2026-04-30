package com.paydayloan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "PDL_NOTIFICATION_LOG")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOTIFICATION_ID")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUEST_ID")
    private LoanRequest loanRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOAN_ID")
    private LoanAccount loanAccount;

    @Column(name = "TARGET_TYPE", nullable = false)
    private String targetType;

    @Column(name = "TARGET_ID", nullable = false)
    private String targetId;

    @Column(name = "CHANNEL", nullable = false)
    private String channel;

    @Column(name = "TEMPLATE_CODE")
    private String templateCode;

    @Column(name = "MESSAGE_SUBJECT")
    private String messageSubject;

    @Column(name = "MESSAGE_BODY")
    private String messageBody;

    @Builder.Default
    @Column(name = "DELIVERY_STATUS")
    private String deliveryStatus = "PENDING";

    @Column(name = "SENT_AT")
    private LocalDateTime sentAt;

    @Column(name = "RESPONSE_CODE")
    private String responseCode;

    @Column(name = "RESPONSE_MESSAGE")
    private String responseMessage;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
