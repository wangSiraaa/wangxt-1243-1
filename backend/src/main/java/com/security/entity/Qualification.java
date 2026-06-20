package com.security.entity;

import com.security.common.enums.QualificationStatus;
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
@Table(name = "sec_qualification")
public class Qualification {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "personnel_id", nullable = false, columnDefinition = "UUID")
    private UUID personnelId;

    @Column(name = "qualification_type_id", nullable = false, columnDefinition = "UUID")
    private UUID qualificationTypeId;

    @Column(name = "certificate_no", nullable = false, length = 100)
    private String certificateNo;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "issuing_authority", length = 100)
    private String issuingAuthority;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'VALID'")
    private QualificationStatus status = QualificationStatus.VALID;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
