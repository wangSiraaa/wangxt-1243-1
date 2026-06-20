package com.security.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "sec_qualification_type")
public class QualificationType {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "type_name", nullable = false, unique = true, length = 50)
    private String typeName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "required_for_key_position", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean requiredForKeyPosition = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
