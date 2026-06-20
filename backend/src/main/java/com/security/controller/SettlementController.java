package com.security.controller;

import com.security.common.Result;
import com.security.common.enums.SettlementStatus;
import com.security.dto.SettlementGenerateDTO;
import com.security.entity.Settlement;
import com.security.entity.SettlementDetail;
import com.security.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/settlement")
public class SettlementController {

    @Autowired
    private SettlementService settlementService;

    @GetMapping
    public Result<List<Settlement>> list(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) SettlementStatus status) {
        return Result.success(settlementService.list(customerId, status));
    }

    @GetMapping("/{id}")
    public Result<Settlement> getById(@PathVariable UUID id) {
        return Result.success(settlementService.getById(id));
    }

    @GetMapping("/{id}/details")
    public Result<List<SettlementDetail>> getDetails(@PathVariable UUID id) {
        return Result.success(settlementService.getSettlementDetails(id));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public Result<Settlement> generate(@RequestBody SettlementGenerateDTO dto) {
        return Result.success(settlementService.generateSettlement(dto.getCustomerId(), dto.getSettlementMonth()));
    }

    @PutMapping("/{id}/confirm")
    public Result<Settlement> confirm(@PathVariable UUID id) {
        return Result.success(settlementService.confirmSettlement(id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public Result<Settlement> updateStatus(@PathVariable UUID id, @RequestParam SettlementStatus status) {
        return Result.success(settlementService.updateStatus(id, status));
    }
}
