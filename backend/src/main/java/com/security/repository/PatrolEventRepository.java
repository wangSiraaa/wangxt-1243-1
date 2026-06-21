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

    @Query(value = "SELECT COUNT(pe.id) FROM sec_patrol_event pe JOIN sec_customer_point cp ON pe.customer_point_id = cp.id WHERE cp.customer_id = :customerId AND pe.customer_confirmed = false", nativeQuery = true)
    Long countUnconfirmedByCustomerId(@Param("customerId") UUID customerId);

    @Query(value = "SELECT pe.* FROM sec_patrol_event pe JOIN sec_customer_point cp ON pe.customer_point_id = cp.id WHERE cp.customer_id = :customerId AND pe.event_time BETWEEN :startTime AND :endTime", nativeQuery = true)
    List<PatrolEvent> findByCustomerIdAndEventTimeBetween(@Param("customerId") UUID customerId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
