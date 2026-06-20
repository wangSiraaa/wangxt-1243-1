package com.security.controller;

import com.security.common.Result;
import com.security.entity.QualificationType;
import com.security.service.QualificationTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/qualification-type")
@PreAuthorize("isAuthenticated()")
public class QualificationTypeController {

    @Autowired
    private QualificationTypeService qualificationTypeService;

    @GetMapping("/")
    public Result<List<QualificationType>> list() {
        List<QualificationType> list = qualificationTypeService.list();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<QualificationType> getById(@PathVariable UUID id) {
        QualificationType qualificationType = qualificationTypeService.getById(id);
        return Result.success(qualificationType);
    }

    @GetMapping("/required-for-key-position")
    public Result<List<QualificationType>> listRequiredForKeyPosition() {
        List<QualificationType> list = qualificationTypeService.listRequiredForKeyPosition();
        return Result.success(list);
    }

    @PostMapping("/")
    public Result<QualificationType> save(@RequestBody QualificationType qualificationType) {
        QualificationType saved = qualificationTypeService.save(qualificationType);
        return Result.success(saved);
    }

    @PutMapping("/")
    public Result<QualificationType> update(@RequestBody QualificationType qualificationType) {
        QualificationType updated = qualificationTypeService.update(qualificationType);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable UUID id) {
        qualificationTypeService.delete(id);
        return Result.success();
    }
}
