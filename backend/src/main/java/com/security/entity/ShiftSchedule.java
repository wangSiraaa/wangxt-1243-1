package com.security.entity;

import com.security.common.enums.ScheduleStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "sec_shift_schedule", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"schedule_date", "customer_point_id", "shift_template_id", "personnel_id"})
})
public class ShiftSchedule {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    @Column(name = "customer_point_id", nullable = false, columnDefinition = "UUID")
    private UUID customerPointId;

    @Column(name = "shift_template_id", nullable = false, columnDefinition = "UUID")
    private UUID shiftTemplateId;

    @Column(name = "personnel_id", nullable = false, columnDefinition = "UUID")
    private UUID personnelId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'SCHEDULED'")
    private ScheduleStatus status = ScheduleStatus.SCHEDULED;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "is_night_shift", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean nightShift = false;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
