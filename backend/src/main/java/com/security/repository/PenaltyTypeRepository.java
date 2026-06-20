package com.security.repository;

import com.security.entity.PenaltyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PenaltyTypeRepository extends JpaRepository<PenaltyType, UUID> {

    List<PenaltyType> findAllByOrderByTypeName();
}
