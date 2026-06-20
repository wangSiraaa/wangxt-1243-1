package com.security.entity;

import com.security.common.enums.Gender;
import com.security.common.enums.PersonnelStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "sec_personnel")
public class Personnel {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "employee_no", unique = true, nullable = false, length = 50)
    private String employeeNo;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(length = 20)
    private String phone;

    @Column(name = "id_card", unique = true, length = 18)
    private String idCard;

    @Column(length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'ACTIVE'")
    private PersonnelStatus status = PersonnelStatus.ACTIVE;

    @Column(name = "max_consecutive_night_shifts", columnDefinition = "INTEGER DEFAULT 3")
    private Integer maxConsecutiveNightShifts = 3;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
