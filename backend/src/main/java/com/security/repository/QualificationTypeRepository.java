package com.security.repository;

import com.security.entity.QualificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QualificationTypeRepository extends JpaRepository<QualificationType, UUID> {

    List<QualificationType> findByRequiredForKeyPositionTrue();
}
