package com.security.repository;

import com.security.common.enums.ShiftType;
import com.security.entity.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, UUID> {

    List<ShiftTemplate> findByShiftType(ShiftType shiftType);
}
