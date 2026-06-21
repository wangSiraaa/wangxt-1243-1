package com.security.entity;

import com.security.common.enums.EventLevel;
import com.security.common.enums.EventStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "sec_patrol_event")
public class PatrolEvent {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "event_no", unique = true, nullable = false, length = 50)
    private String eventNo;

    @Column(name = "customer_point_id", nullable = false, columnDefinition = "UUID")
    private UUID customerPointId;

    @Column(name = "schedule_id", columnDefinition = "UUID")
    private UUID scheduleId;

    @Column(name = "reporter_id", nullable = false, columnDefinition = "UUID")
    private UUID reporterId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_level", nullable = false, length = 20)
    private EventLevel eventLevel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Column(name = "customer_confirmed", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean customerConfirmed = false;

    @Column(name = "customer_confirmer_id", columnDefinition = "UUID")
    private UUID customerConfirmerId;

    @Column(name = "customer_confirmed_at")
    private LocalDateTime customerConfirmedAt;

    @Column(name = "customer_remarks", columnDefinition = "TEXT")
    private String customerRemarks;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'OPEN'")
    private EventStatus status = EventStatus.OPEN;

    @Transient
    private ShiftExchange exchangeInfo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
