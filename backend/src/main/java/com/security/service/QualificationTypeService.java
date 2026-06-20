package com.security.service;

import com.security.common.exception.BusinessException;
import com.security.entity.QualificationType;
import com.security.repository.QualificationTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class QualificationTypeService {

    @Autowired
    private QualificationTypeRepository qualificationTypeRepository;

    public QualificationType getById(UUID id) {
        if (id == null) {
            throw new BusinessException("资质类型ID不能为空");
        }
        return qualificationTypeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("资质类型不存在，ID: " + id));
    }

    public List<QualificationType> list() {
        return qualificationTypeRepository.findAll();
    }

    public List<QualificationType> listRequiredForKeyPosition() {
        return qualificationTypeRepository.findByRequiredForKeyPositionTrue();
    }

    public QualificationType save(QualificationType qualificationType) {
        validateQualificationType(qualificationType);

        if (qualificationType.getRequiredForKeyPosition() == null) {
            qualificationType.setRequiredForKeyPosition(false);
        }

        return qualificationTypeRepository.save(qualificationType);
    }

    public QualificationType update(QualificationType qualificationType) {
        if (qualificationType.getId() == null) {
            throw new BusinessException("资质类型ID不能为空");
        }

        QualificationType existing = getById(qualificationType.getId());
        validateQualificationType(qualificationType);

        existing.setTypeName(qualificationType.getTypeName());
        existing.setDescription(qualificationType.getDescription());
        existing.setRequiredForKeyPosition(qualificationType.getRequiredForKeyPosition());

        return qualificationTypeRepository.save(existing);
    }

    public void delete(UUID id) {
        QualificationType qualificationType = getById(id);
        qualificationTypeRepository.delete(qualificationType);
    }

    private void validateQualificationType(QualificationType qualificationType) {
        if (!StringUtils.hasText(qualificationType.getTypeName())) {
            throw new BusinessException("资质类型名称不能为空");
        }
        if (qualificationType.getTypeName().length() > 50) {
            throw new BusinessException("资质类型名称长度不能超过50");
        }
    }
}
