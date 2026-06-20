package com.security.entity;

import com.security.common.enums.SettlementStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "sec_settlement")
public class Settlement {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "settlement_no", unique = true, nullable = false, length = 50)
    private String settlementNo;

    @Column(name = "customer_id", nullable = false, columnDefinition = "UUID")
    private UUID customerId;

    @Column(name = "settlement_month", nullable = false, length = 7)
    private String settlementMonth;

    @Column(name = "total_shifts", columnDefinition = "INTEGER DEFAULT 0")
    private Integer totalShifts = 0;

    @Column(name = "total_amount", precision = 12, scale = 2, columnDefinition = "NUMERIC(12,2) DEFAULT 0")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "penalty_amount", precision = 10, scale = 2, columnDefinition = "NUMERIC(10,2) DEFAULT 0")
    private BigDecimal penaltyAmount = BigDecimal.ZERO;

    @Column(name = "actual_amount", precision = 12, scale = 2, columnDefinition = "NUMERIC(12,2) DEFAULT 0")
    private BigDecimal actualAmount = BigDecimal.ZERO;

    @Column(name = "unconfirmed_event_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer unconfirmedEventCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'DRAFT'")
    private SettlementStatus status = SettlementStatus.DRAFT;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
