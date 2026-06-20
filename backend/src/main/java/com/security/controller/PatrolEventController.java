package com.security.controller;

import com.security.common.LoginUser;
import com.security.common.Result;
import com.security.common.enums.EventStatus;
import com.security.dto.EventConfirmDTO;
import com.security.entity.PatrolEvent;
import com.security.service.PatrolEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patrol-event")
public class PatrolEventController {

    @Autowired
    private PatrolEventService patrolEventService;

    @GetMapping
    public Result<List<PatrolEvent>> list(
            @RequestParam(required = false) UUID pointId,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(patrolEventService.list(pointId, status, startTime, endTime));
    }

    @GetMapping("/{id}")
    public Result<PatrolEvent> getById(@PathVariable UUID id) {
        return Result.success(patrolEventService.getById(id));
    }

    @GetMapping("/unconfirmed")
    public Result<List<PatrolEvent>> listUnconfirmed() {
        return Result.success(patrolEventService.listUnconfirmed());
    }

    @GetMapping("/unconfirmed/count")
    public Result<Long> countUnconfirmed(@RequestParam UUID customerId) {
        return Result.success(patrolEventService.countUnconfirmedByCustomer(customerId));
    }

    @PostMapping
    @PreAuthorize("hasRole('TEAM_LEADER')")
    public Result<PatrolEvent> save(@RequestBody PatrolEvent event, @AuthenticationPrincipal LoginUser loginUser) {
        event.setReporterId(loginUser.getId());
        return Result.success(patrolEventService.save(event));
    }

    @PutMapping
    public Result<PatrolEvent> update(@RequestBody PatrolEvent event) {
        return Result.success(patrolEventService.update(event));
    }

    @PutMapping("/{id}/status")
    public Result<PatrolEvent> updateStatus(@PathVariable UUID id, @RequestParam EventStatus status) {
        return Result.success(patrolEventService.updateStatus(id, status));
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<PatrolEvent> confirm(
            @PathVariable UUID id,
            @RequestBody(required = false) EventConfirmDTO dto,
            @AuthenticationPrincipal LoginUser loginUser) {
        String remarks = dto != null ? dto.getRemarks() : null;
        return Result.success(patrolEventService.confirmByCustomer(id, loginUser.getId(), remarks));
    }
}
