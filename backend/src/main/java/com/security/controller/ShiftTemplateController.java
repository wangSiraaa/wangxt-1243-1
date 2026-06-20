package com.security.controller;

import com.security.common.Result;
import com.security.common.enums.ShiftType;
import com.security.entity.ShiftTemplate;
import com.security.service.ShiftTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/shift-template")
@PreAuthorize("isAuthenticated()")
public class ShiftTemplateController {

    @Autowired
    private ShiftTemplateService shiftTemplateService;

    @GetMapping("/")
    public Result<List<ShiftTemplate>> list() {
        List<ShiftTemplate> list = shiftTemplateService.list();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<ShiftTemplate> getById(@PathVariable UUID id) {
        ShiftTemplate shiftTemplate = shiftTemplateService.getById(id);
        return Result.success(shiftTemplate);
    }

    @GetMapping("/type/{shiftType}")
    public Result<List<ShiftTemplate>> listByShiftType(@PathVariable ShiftType shiftType) {
        List<ShiftTemplate> list = shiftTemplateService.listByShiftType(shiftType);
        return Result.success(list);
    }

    @PostMapping("/")
    public Result<ShiftTemplate> save(@RequestBody ShiftTemplate shiftTemplate) {
        ShiftTemplate saved = shiftTemplateService.save(shiftTemplate);
        return Result.success(saved);
    }

    @PutMapping("/")
    public Result<ShiftTemplate> update(@RequestBody ShiftTemplate shiftTemplate) {
        ShiftTemplate updated = shiftTemplateService.update(shiftTemplate);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable UUID id) {
        shiftTemplateService.delete(id);
        return Result.success();
    }
}
