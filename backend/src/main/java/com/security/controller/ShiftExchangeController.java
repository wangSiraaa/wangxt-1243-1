package com.security.controller;

import com.security.common.LoginUser;
import com.security.common.Result;
import com.security.dto.ExchangeApproveDTO;
import com.security.entity.ShiftExchange;
import com.security.service.ShiftExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/shift-exchange")
public class ShiftExchangeController {

    @Autowired
    private ShiftExchangeService shiftExchangeService;

    @GetMapping
    public Result<List<ShiftExchange>> list() {
        return Result.success(shiftExchangeService.listAll());
    }

    @GetMapping("/pending")
    public Result<List<ShiftExchange>> listPending() {
        return Result.success(shiftExchangeService.listPendingExchanges());
    }

    @GetMapping("/{id}")
    public Result<ShiftExchange> getById(@PathVariable UUID id) {
        return Result.success(shiftExchangeService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('TEAM_LEADER')")
    public Result<ShiftExchange> create(@RequestBody ShiftExchange exchange, @AuthenticationPrincipal LoginUser loginUser) {
        exchange.setRequesterId(loginUser.getId());
        return Result.success(shiftExchangeService.createExchange(exchange));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public Result<ShiftExchange> approve(
            @PathVariable UUID id,
            @RequestBody ExchangeApproveDTO dto,
            @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(shiftExchangeService.approveExchange(id, loginUser.getId(), dto.getApprovalRemarks()));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public Result<ShiftExchange> reject(
            @PathVariable UUID id,
            @RequestBody ExchangeApproveDTO dto,
            @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(shiftExchangeService.rejectExchange(id, loginUser.getId(), dto.getApprovalRemarks()));
    }
}
