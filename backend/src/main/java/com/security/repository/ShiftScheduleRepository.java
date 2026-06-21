package com.security.repository;

import com.security.common.enums.ScheduleStatus;
import com.security.entity.ShiftSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShiftScheduleRepository extends JpaRepository<ShiftSchedule, UUID> {

    List<ShiftSchedule> findByScheduleDateBetween(LocalDate startDate, LocalDate endDate);

    List<ShiftSchedule> findByPersonnelIdAndScheduleDateBetween(UUID personnelId, LocalDate startDate, LocalDate endDate);

    List<ShiftSchedule> findByCustomerPointIdAndScheduleDateBetween(UUID customerPointId, LocalDate startDate, LocalDate endDate);

    List<ShiftSchedule> findByNightShiftTrueAndPersonnelId(UUID personnelId);

    @Query(value = "SELECT COUNT(ss.id) FROM sec_shift_schedule ss JOIN sec_customer_point cp ON ss.customer_point_id = cp.id WHERE cp.customer_id = :customerId AND ss.schedule_date BETWEEN :startDate AND :endDate AND ss.status = :status", nativeQuery = true)
    Long countByCustomerIdAndScheduleDateBetweenAndStatus(@Param("customerId") UUID customerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("status") ScheduleStatus status);
}
