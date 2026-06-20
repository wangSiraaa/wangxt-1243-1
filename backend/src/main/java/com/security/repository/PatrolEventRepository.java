package com.security.repository;

import com.security.common.enums.EventStatus;
import com.security.entity.PatrolEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PatrolEventRepository extends JpaRepository<PatrolEvent, UUID> {

    List<PatrolEvent> findByCustomerPointId(UUID customerPointId);

    List<PatrolEvent> findByCustomerConfirmedFalse();

    List<PatrolEvent> findByEventTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    List<PatrolEvent> findByStatus(EventStatus status);

    @Query("SELECT COUNT(pe) FROM PatrolEvent pe JOIN CustomerPoint cp ON pe.customerPointId = cp.id WHERE cp.customerId = :customerId AND pe.customerConfirmed = false")
    Long countUnconfirmedByCustomerId(@Param("customerId") UUID customerId);

    @Query("SELECT pe FROM PatrolEvent pe JOIN CustomerPoint cp ON pe.customerPointId = cp.id WHERE cp.customerId = :customerId AND pe.eventTime BETWEEN :startTime AND :endTime")
    List<PatrolEvent> findByCustomerIdAndEventTimeBetween(@Param("customerId") UUID customerId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
