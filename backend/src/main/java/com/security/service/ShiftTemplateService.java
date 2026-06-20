package com.security.service;

import com.security.common.enums.ShiftType;
import com.security.common.exception.BusinessException;
import com.security.entity.ShiftTemplate;
import com.security.repository.ShiftTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class ShiftTemplateService {

    @Autowired
    private ShiftTemplateRepository shiftTemplateRepository;

    public ShiftTemplate getById(UUID id) {
        if (id == null) {
            throw new BusinessException("班次模板ID不能为空");
        }
        return shiftTemplateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("班次模板不存在，ID: " + id));
    }

    public List<ShiftTemplate> list() {
        return shiftTemplateRepository.findAll();
    }

    public List<ShiftTemplate> listByShiftType(ShiftType shiftType) {
        if (shiftType == null) {
            throw new BusinessException("班次类型不能为空");
        }
        return shiftTemplateRepository.findByShiftType(shiftType);
    }

    public ShiftTemplate save(ShiftTemplate shiftTemplate) {
        validateShiftTemplate(shiftTemplate);
        
        if (shiftTemplate.getDurationHours() == null) {
            shiftTemplate.setDurationHours(calculateDurationHours(
                    shiftTemplate.getStartTime(), 
                    shiftTemplate.getEndTime()));
        }
        
        return shiftTemplateRepository.save(shiftTemplate);
    }

    public ShiftTemplate update(ShiftTemplate shiftTemplate) {
        if (shiftTemplate.getId() == null) {
            throw new BusinessException("班次模板ID不能为空");
        }
        
        ShiftTemplate existing = getById(shiftTemplate.getId());
        validateShiftTemplate(shiftTemplate);
        
        existing.setTemplateName(shiftTemplate.getTemplateName());
        existing.setShiftType(shiftTemplate.getShiftType());
        existing.setStartTime(shiftTemplate.getStartTime());
        existing.setEndTime(shiftTemplate.getEndTime());
        existing.setDurationHours(calculateDurationHours(
                shiftTemplate.getStartTime(), 
                shiftTemplate.getEndTime()));
        existing.setDescription(shiftTemplate.getDescription());
        
        return shiftTemplateRepository.save(existing);
    }

    public void delete(UUID id) {
        ShiftTemplate shiftTemplate = getById(id);
        shiftTemplateRepository.delete(shiftTemplate);
    }

    public BigDecimal calculateDurationHours(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BusinessException("开始时间和结束时间不能为空");
        }
        
        Duration duration;
        if (endTime.isAfter(startTime)) {
            duration = Duration.between(startTime, endTime);
        } else {
            duration = Duration.between(startTime, LocalTime.MAX)
                    .plus(Duration.between(LocalTime.MIN, endTime))
                    .plusHours(1);
        }
        
        long minutes = duration.toMinutes();
        return BigDecimal.valueOf(minutes / 60.0).setScale(1, RoundingMode.HALF_UP);
    }

    public boolean isNightShift(LocalTime startTime, LocalTime endTime) {
        LocalTime nightStart = LocalTime.of(22, 0);
        LocalTime nightEnd = LocalTime.of(6, 0);
        
        if (startTime.isBefore(endTime)) {
            return (startTime.isBefore(nightEnd) && endTime.isAfter(nightStart))
                    || (startTime.isAfter(nightStart) && endTime.isBefore(nightEnd));
        } else {
            return true;
        }
    }

    private void validateShiftTemplate(ShiftTemplate shiftTemplate) {
        if (!StringUtils.hasText(shiftTemplate.getTemplateName())) {
            throw new BusinessException("模板名称不能为空");
        }
        if (shiftTemplate.getTemplateName().length() > 50) {
            throw new BusinessException("模板名称长度不能超过50");
        }
        if (shiftTemplate.getShiftType() == null) {
            throw new BusinessException("班次类型不能为空");
        }
        if (shiftTemplate.getStartTime() == null) {
            throw new BusinessException("开始时间不能为空");
        }
        if (shiftTemplate.getEndTime() == null) {
            throw new BusinessException("结束时间不能为空");
        }
        if (shiftTemplate.getDurationHours() != null 
                && shiftTemplate.getDurationHours().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("工时必须大于0");
        }
        if (shiftTemplate.getDurationHours() != null 
                && shiftTemplate.getDurationHours().compareTo(new BigDecimal("24")) > 0) {
            throw new BusinessException("工时不能超过24小时");
        }
    }
}
