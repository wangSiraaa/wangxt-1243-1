package com.security.entity;

import com.security.common.enums.ExchangeStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "sec_shift_exchange")
public class ShiftExchange {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "original_schedule_id", nullable = false, columnDefinition = "UUID")
    private UUID originalScheduleId;

    @Column(name = "requester_id", nullable = false, columnDefinition = "UUID")
    private UUID requesterId;

    @Column(name = "replacement_id", nullable = false, columnDefinition = "UUID")
    private UUID replacementId;

    @Column(name = "exchange_reason", columnDefinition = "TEXT")
    private String exchangeReason;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'PENDING'")
    private ExchangeStatus status = ExchangeStatus.PENDING;

    @Column(name = "approver_id", columnDefinition = "UUID")
    private UUID approverId;

    @Column(name = "approval_remarks", columnDefinition = "TEXT")
    private String approvalRemarks;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
