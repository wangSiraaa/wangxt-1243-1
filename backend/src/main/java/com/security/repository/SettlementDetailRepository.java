package com.security.repository;

import com.security.entity.SettlementDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SettlementDetailRepository extends JpaRepository<SettlementDetail, UUID> {

    List<SettlementDetail> findBySettlementId(UUID settlementId);
}
