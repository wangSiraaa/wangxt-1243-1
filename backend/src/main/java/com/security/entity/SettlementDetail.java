package com.security.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "sec_settlement_detail")
public class SettlementDetail {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "settlement_id", nullable = false, columnDefinition = "UUID")
    private UUID settlementId;

    @Column(name = "schedule_id", nullable = false, columnDefinition = "UUID")
    private UUID scheduleId;

    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;

    @Column(name = "shift_amount", precision = 10, scale = 2, columnDefinition = "NUMERIC(10,2) DEFAULT 0")
    private BigDecimal shiftAmount = BigDecimal.ZERO;

    @Column(name = "included_in_settlement", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean includedInSettlement = true;

    @Column(name = "exclusion_reason", columnDefinition = "TEXT")
    private String exclusionReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
