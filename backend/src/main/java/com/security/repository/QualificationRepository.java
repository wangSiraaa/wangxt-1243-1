package com.security.repository;

import com.security.common.enums.QualificationStatus;
import com.security.entity.Qualification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface QualificationRepository extends JpaRepository<Qualification, UUID> {

    List<Qualification> findByPersonnelId(UUID personnelId);

    List<Qualification> findByStatus(QualificationStatus status);

    List<Qualification> findByExpiryDateBefore(LocalDate date);
}
