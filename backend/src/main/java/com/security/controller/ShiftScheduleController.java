package com.security.controller;

import com.security.common.LoginUser;
import com.security.common.Result;
import com.security.dto.ScheduleBatchSaveDTO;
import com.security.entity.ShiftSchedule;
import com.security.service.ShiftScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/shift-schedule")
public class ShiftScheduleController {

    @Autowired
    private ShiftScheduleService shiftScheduleService;

    @GetMapping
    public Result<List<ShiftSchedule>> list(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return Result.success(shiftScheduleService.listByDateRange(startDate, endDate));
        }
        return Result.success(shiftScheduleService.listByDateRange(
                LocalDate.now().withDayOfMonth(1),
                LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())));
    }

    @GetMapping("/{id}")
    public Result<ShiftSchedule> getById(@PathVariable UUID id) {
        return Result.success(shiftScheduleService.getById(id));
    }

    @GetMapping("/personnel/{personnelId}")
    public Result<List<ShiftSchedule>> listByPersonnel(
            @PathVariable UUID personnelId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        return Result.success(shiftScheduleService.listByPersonnelAndDateRange(personnelId, start, end));
    }

    @GetMapping("/point/{customerPointId}")
    public Result<List<ShiftSchedule>> listByPoint(
            @PathVariable UUID customerPointId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        return Result.success(shiftScheduleService.listByPointAndDateRange(customerPointId, start, end));
    }

    @GetMapping("/calendar")
    public Result<Map<LocalDate, List<ShiftSchedule>>> getCalendar(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(shiftScheduleService.getCalendarView(startDate, endDate));
    }

    @PostMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public Result<ShiftSchedule> save(@RequestBody ShiftSchedule schedule) {
        return Result.success(shiftScheduleService.save(schedule));
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public Result<List<ShiftSchedule>> batchSave(@RequestBody ScheduleBatchSaveDTO dto) {
        return Result.success(shiftScheduleService.batchSave(dto.getSchedules()));
    }

    @PutMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public Result<ShiftSchedule> update(@RequestBody ShiftSchedule schedule) {
        return Result.success(shiftScheduleService.update(schedule));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public Result<Void> delete(@PathVariable UUID id) {
        shiftScheduleService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/check-in")
    @PreAuthorize("hasRole('TEAM_LEADER')")
    public Result<ShiftSchedule> checkIn(@PathVariable UUID id, @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(shiftScheduleService.checkIn(id));
    }

    @PutMapping("/{id}/check-out")
    @PreAuthorize("hasRole('TEAM_LEADER')")
    public Result<ShiftSchedule> checkOut(@PathVariable UUID id, @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(shiftScheduleService.checkOut(id));
    }
}
