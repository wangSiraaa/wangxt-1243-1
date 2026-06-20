package com.security.controller;

import com.security.common.Result;
import com.security.common.enums.PersonnelStatus;
import com.security.entity.Personnel;
import com.security.service.PersonnelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/personnel")
@PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'TEAM_LEADER')")
public class PersonnelController {

    @Autowired
    private PersonnelService personnelService;

    @GetMapping("/")
    public Result<List<Personnel>> list() {
        List<Personnel> list = personnelService.list();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<Personnel> getById(@PathVariable UUID id) {
        Personnel personnel = personnelService.getById(id);
        return Result.success(personnel);
    }

    @PostMapping("/")
    public Result<Personnel> save(@RequestBody Personnel personnel) {
        Personnel saved = personnelService.save(personnel);
        return Result.success(saved);
    }

    @PutMapping("/")
    public Result<Personnel> update(@RequestBody Personnel personnel) {
        Personnel updated = personnelService.update(personnel);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable UUID id) {
        personnelService.delete(id);
        return Result.success();
    }

    @GetMapping("/status/{status}")
    public Result<List<Personnel>> listByStatus(@PathVariable PersonnelStatus status) {
        List<Personnel> list = personnelService.listByStatus(status);
        return Result.success(list);
    }
}
