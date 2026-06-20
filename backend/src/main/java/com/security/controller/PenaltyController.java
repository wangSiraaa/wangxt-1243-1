package com.security.controller;

import com.security.common.Result;
import com.security.common.enums.PenaltyStatus;
import com.security.entity.Penalty;
import com.security.service.PenaltyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/penalty")
public class PenaltyController {

    @Autowired
    private PenaltyService penaltyService;

    @GetMapping
    public Result<List<Penalty>> list(
            @RequestParam(required = false) UUID personnelId,
            @RequestParam(required = false) PenaltyStatus status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(penaltyService.list(personnelId, status, startDate, endDate));
    }

    @GetMapping("/{id}")
    public Result<Penalty> getById(@PathVariable UUID id) {
        return Result.success(penaltyService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public Result<Penalty> save(@RequestBody Penalty penalty) {
        return Result.success(penaltyService.save(penalty));
    }

    @PutMapping
    public Result<Penalty> update(@RequestBody Penalty penalty) {
        return Result.success(penaltyService.update(penalty));
    }

    @PutMapping("/{id}/status")
    public Result<Penalty> updateStatus(@PathVariable UUID id, @RequestParam PenaltyStatus status) {
        return Result.success(penaltyService.updateStatus(id, status));
    }

    @GetMapping("/total")
    public Result<BigDecimal> getTotalPenalty(
            @RequestParam UUID personnelId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(penaltyService.calculateTotalPenaltyByPersonnelAndDateRange(personnelId, startDate, endDate));
    }
}
