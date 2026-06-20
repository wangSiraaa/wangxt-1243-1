package com.security.service;

import com.security.common.enums.QualificationStatus;
import com.security.common.exception.BusinessException;
import com.security.entity.Qualification;
import com.security.repository.QualificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

@Service
public class QualificationService {

    private static final int EXPIRING_WARNING_DAYS = 30;

    @Autowired
    private QualificationRepository qualificationRepository;

    public Qualification getById(UUID id) {
        if (id == null) {
            throw new BusinessException("资质ID不能为空");
        }
        return qualificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("资质不存在，ID: " + id));
    }

    public List<Qualification> list() {
        return qualificationRepository.findAll();
    }

    public List<Qualification> listByPersonnelId(UUID personnelId) {
        if (personnelId == null) {
            throw new BusinessException("人员ID不能为空");
        }
        return qualificationRepository.findByPersonnelId(personnelId);
    }

    public List<Qualification> listByStatus(QualificationStatus status) {
        if (status == null) {
            throw new BusinessException("状态不能为空");
        }
        return qualificationRepository.findByStatus(status);
    }

    public Qualification save(Qualification qualification) {
        validateQualification(qualification);
        
        if (qualification.getStatus() == null) {
            qualification.setStatus(calculateStatus(qualification.getExpiryDate()));
        }
        
        return qualificationRepository.save(qualification);
    }

    public Qualification update(Qualification qualification) {
        if (qualification.getId() == null) {
            throw new BusinessException("资质ID不能为空");
        }
        
        Qualification existing = getById(qualification.getId());
        validateQualification(qualification);
        
        existing.setPersonnelId(qualification.getPersonnelId());
        existing.setQualificationTypeId(qualification.getQualificationTypeId());
        existing.setCertificateNo(qualification.getCertificateNo());
        existing.setIssueDate(qualification.getIssueDate());
        existing.setExpiryDate(qualification.getExpiryDate());
        existing.setIssuingAuthority(qualification.getIssuingAuthority());
        existing.setStatus(calculateStatus(qualification.getExpiryDate()));
        
        return qualificationRepository.save(existing);
    }

    public void delete(UUID id) {
        Qualification qualification = getById(id);
        qualificationRepository.delete(qualification);
    }

    public QualificationStatus calculateStatus(LocalDate expiryDate) {
        if (expiryDate == null) {
            throw new BusinessException("过期日期不能为空");
        }
        LocalDate today = LocalDate.now();
        if (expiryDate.isBefore(today)) {
            return QualificationStatus.EXPIRED;
        } else if (expiryDate.isBefore(today.plusDays(EXPIRING_WARNING_DAYS))) {
            return QualificationStatus.EXPIRING;
        } else {
            return QualificationStatus.VALID;
        }
    }

    public void updateAllQualificationStatuses() {
        List<Qualification> qualifications = qualificationRepository.findAll();
        for (Qualification qualification : qualifications) {
            QualificationStatus newStatus = calculateStatus(qualification.getExpiryDate());
            if (newStatus != qualification.getStatus()) {
                qualification.setStatus(newStatus);
                qualificationRepository.save(qualification);
            }
        }
    }

    public void updateStatuses(List<UUID> ids, QualificationStatus status) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("资质ID列表不能为空");
        }
        if (status == null) {
            throw new BusinessException("状态不能为空");
        }
        List<Qualification> qualifications = qualificationRepository.findAllById(ids);
        for (Qualification qualification : qualifications) {
            if (status != qualification.getStatus()) {
                qualification.setStatus(status);
                qualificationRepository.save(qualification);
            }
        }
    }

    public List<Qualification> getExpiringQualifications() {
        LocalDate warningDate = LocalDate.now().plusDays(EXPIRING_WARNING_DAYS);
        return qualificationRepository.findByExpiryDateBefore(warningDate);
    }

    public Qualification updateStatus(UUID id) {
        Qualification qualification = getById(id);
        QualificationStatus newStatus = calculateStatus(qualification.getExpiryDate());
        if (newStatus != qualification.getStatus()) {
            qualification.setStatus(newStatus);
            return qualificationRepository.save(qualification);
        }
        return qualification;
    }

    private void validateQualification(Qualification qualification) {
        if (qualification.getPersonnelId() == null) {
            throw new BusinessException("人员ID不能为空");
        }
        if (qualification.getQualificationTypeId() == null) {
            throw new BusinessException("资质类型ID不能为空");
        }
        if (!StringUtils.hasText(qualification.getCertificateNo())) {
            throw new BusinessException("证书编号不能为空");
        }
        if (qualification.getCertificateNo().length() > 100) {
            throw new BusinessException("证书编号长度不能超过100");
        }
        if (qualification.getIssueDate() == null) {
            throw new BusinessException("发证日期不能为空");
        }
        if (qualification.getExpiryDate() == null) {
            throw new BusinessException("过期日期不能为空");
        }
        if (qualification.getExpiryDate().isBefore(qualification.getIssueDate())) {
            throw new BusinessException("过期日期不能早于发证日期");
        }
        if (qualification.getIssuingAuthority() != null && qualification.getIssuingAuthority().length() > 100) {
            throw new BusinessException("发证机关长度不能超过100");
        }
    }
}
