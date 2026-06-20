package com.security.entity;

import com.security.common.enums.PenaltyStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "sec_penalty")
public class Penalty {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "penalty_no", unique = true, nullable = false, length = 50)
    private String penaltyNo;

    @Column(name = "personnel_id", nullable = false, columnDefinition = "UUID")
    private UUID personnelId;

    @Column(name = "penalty_type_id", nullable = false, columnDefinition = "UUID")
    private UUID penaltyTypeId;

    @Column(name = "patrol_event_id", columnDefinition = "UUID")
    private UUID patrolEventId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "penalty_date", nullable = false)
    private LocalDate penaltyDate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'UNPAID'")
    private PenaltyStatus status = PenaltyStatus.UNPAID;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
