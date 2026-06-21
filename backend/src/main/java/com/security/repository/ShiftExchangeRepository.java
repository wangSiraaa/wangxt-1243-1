package com.security.repository;

import com.security.common.enums.ExchangeStatus;
import com.security.entity.ShiftExchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShiftExchangeRepository extends JpaRepository<ShiftExchange, UUID> {

    List<ShiftExchange> findByStatus(ExchangeStatus status);

    List<ShiftExchange> findByRequesterId(UUID requesterId);

    ShiftExchange findByOriginalScheduleId(UUID originalScheduleId);

    ShiftExchange findByOriginalScheduleIdAndStatus(UUID originalScheduleId, ExchangeStatus status);
}
