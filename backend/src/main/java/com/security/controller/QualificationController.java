package com.security.controller;

import com.security.common.Result;
import com.security.common.enums.QualificationStatus;
import com.security.entity.Qualification;
import com.security.service.QualificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/qualification")
@PreAuthorize("isAuthenticated()")
public class QualificationController {

    @Autowired
    private QualificationService qualificationService;

    @GetMapping("/")
    public Result<List<Qualification>> list() {
        List<Qualification> list = qualificationService.list();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<Qualification> getById(@PathVariable UUID id) {
        Qualification qualification = qualificationService.getById(id);
        return Result.success(qualification);
    }

    @GetMapping("/personnel/{personnelId}")
    public Result<List<Qualification>> listByPersonnelId(@PathVariable UUID personnelId) {
        List<Qualification> list = qualificationService.listByPersonnelId(personnelId);
        return Result.success(list);
    }

    @GetMapping("/status/{status}")
    public Result<List<Qualification>> listByStatus(@PathVariable QualificationStatus status) {
        List<Qualification> list = qualificationService.listByStatus(status);
        return Result.success(list);
    }

    @PostMapping("/")
    public Result<Qualification> save(@RequestBody Qualification qualification) {
        Qualification saved = qualificationService.save(qualification);
        return Result.success(saved);
    }

    @PutMapping("/")
    public Result<Qualification> update(@RequestBody Qualification qualification) {
        Qualification updated = qualificationService.update(qualification);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable UUID id) {
        qualificationService.delete(id);
        return Result.success();
    }

    @PostMapping("/update-statuses")
    public Result<Void> updateStatuses(@RequestBody Map<String, Object> params) {
        if (params.containsKey("ids") && params.containsKey("status")) {
            @SuppressWarnings("unchecked")
            List<String> idStrings = (List<String>) params.get("ids");
            List<UUID> ids = new ArrayList<>();
            for (String idStr : idStrings) {
                ids.add(UUID.fromString(idStr));
            }
            QualificationStatus status = QualificationStatus.valueOf((String) params.get("status"));
            qualificationService.updateStatuses(ids, status);
            return Result.success("资质状态批量更新成功", null);
        } else {
            qualificationService.updateAllQualificationStatuses();
            return Result.success("所有资质状态更新成功", null);
        }
    }
}
