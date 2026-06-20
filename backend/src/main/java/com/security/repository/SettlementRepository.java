package com.security.repository;

import com.security.common.enums.SettlementStatus;
import com.security.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, UUID> {

    List<Settlement> findByCustomerId(UUID customerId);

    Settlement findBySettlementMonth(String settlementMonth);

    Settlement findByCustomerIdAndSettlementMonth(UUID customerId, String settlementMonth);

    List<Settlement> findByStatus(SettlementStatus status);
}
