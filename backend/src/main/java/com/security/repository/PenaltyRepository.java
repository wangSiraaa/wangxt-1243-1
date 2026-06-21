package com.security.repository;

import com.security.common.enums.PenaltyStatus;
import com.security.entity.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PenaltyRepository extends JpaRepository<Penalty, UUID> {

    List<Penalty> findByPersonnelId(UUID personnelId);

    List<Penalty> findByStatus(PenaltyStatus status);

    List<Penalty> findByPenaltyDateBetween(LocalDate startDate, LocalDate endDate);

    List<Penalty> findByPersonnelIdAndPenaltyDateBetween(UUID personnelId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Penalty p WHERE p.personnelId = :personnelId AND p.penaltyDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalPenaltyByPersonnelAndDateRange(@Param("personnelId") UUID personnelId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT COALESCE(SUM(p.amount), 0) FROM sec_penalty p JOIN sec_patrol_event pe ON p.patrol_event_id = pe.id JOIN sec_customer_point cp ON pe.customer_point_id = cp.id WHERE cp.customer_id = :customerId AND p.penalty_date BETWEEN :startDate AND :endDate", nativeQuery = true)
    BigDecimal calculateTotalPenaltyByCustomerAndDateRange(@Param("customerId") UUID customerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
