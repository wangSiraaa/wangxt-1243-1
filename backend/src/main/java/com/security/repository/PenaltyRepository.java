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

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Penalty p JOIN ShiftSchedule s ON p.patrolEventId = s.id JOIN CustomerPoint cp ON s.customerPointId = cp.id WHERE cp.customerId = :customerId AND p.penaltyDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalPenaltyByCustomerAndDateRange(@Param("customerId") UUID customerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
