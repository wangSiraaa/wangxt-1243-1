package com.security.service;

import com.security.common.enums.PenaltyStatus;
import com.security.common.exception.BusinessException;
import com.security.entity.Penalty;
import com.security.repository.PenaltyRepository;
import com.security.repository.PenaltyTypeRepository;
import com.security.repository.PersonnelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PenaltyService {

    @Autowired
    private PenaltyRepository penaltyRepository;

    @Autowired
    private PenaltyTypeRepository penaltyTypeRepository;

    @Autowired
    private PersonnelRepository personnelRepository;

    public Penalty getById(UUID id) {
        if (id == null) {
            throw new BusinessException("扣罚记录ID不能为空");
        }
        return penaltyRepository.findById(id)
                .orElseThrow(() -> new BusinessException("扣罚记录不存在，ID: " + id));
    }

    public List<Penalty> listByPersonnel(UUID personnelId) {
        if (personnelId == null) {
            throw new BusinessException("人员ID不能为空");
        }
        return penaltyRepository.findByPersonnelId(personnelId);
    }

    public List<Penalty> listByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessException("开始日期和结束日期不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        return penaltyRepository.findByPenaltyDateBetween(startDate, endDate);
    }

    public List<Penalty> listByStatus(PenaltyStatus status) {
        if (status == null) {
            throw new BusinessException("扣罚状态不能为空");
        }
        return penaltyRepository.findByStatus(status);
    }

    public List<Penalty> list(UUID personnelId, PenaltyStatus status, LocalDate startDate, LocalDate endDate) {
        List<Penalty> penalties = penaltyRepository.findAll();
        
        if (personnelId != null) {
            penalties = penalties.stream()
                    .filter(p -> p.getPersonnelId().equals(personnelId))
                    .collect(Collectors.toList());
        }
        
        if (status != null) {
            penalties = penalties.stream()
                    .filter(p -> p.getStatus() == status)
                    .collect(Collectors.toList());
        }
        
        if (startDate != null) {
            penalties = penalties.stream()
                    .filter(p -> !p.getPenaltyDate().isBefore(startDate))
                    .collect(Collectors.toList());
        }
        
        if (endDate != null) {
            penalties = penalties.stream()
                    .filter(p -> !p.getPenaltyDate().isAfter(endDate))
                    .collect(Collectors.toList());
        }
        
        return penalties;
    }

    public Penalty save(Penalty penalty) {
        validatePenalty(penalty);

        if (penalty.getStatus() == null) {
            penalty.setStatus(PenaltyStatus.UNPAID);
        }

        String penaltyNo = generatePenaltyNo();
        penalty.setPenaltyNo(penaltyNo);

        return penaltyRepository.save(penalty);
    }

    public Penalty update(Penalty penalty) {
        if (penalty.getId() == null) {
            throw new BusinessException("扣罚记录ID不能为空");
        }

        Penalty existing = getById(penalty.getId());
        validatePenalty(penalty);

        existing.setPenaltyTypeId(penalty.getPenaltyTypeId());
        existing.setPatrolEventId(penalty.getPatrolEventId());
        existing.setAmount(penalty.getAmount());
        existing.setPenaltyDate(penalty.getPenaltyDate());
        existing.setReason(penalty.getReason());
        existing.setStatus(penalty.getStatus());

        return penaltyRepository.save(existing);
    }

    public Penalty updateStatus(UUID id, PenaltyStatus status) {
        if (status == null) {
            throw new BusinessException("扣罚状态不能为空");
        }
        Penalty penalty = getById(id);
        penalty.setStatus(status);
        return penaltyRepository.save(penalty);
    }

    public BigDecimal calculateTotalPenaltyByPersonnelAndDateRange(UUID personnelId, LocalDate startDate, LocalDate endDate) {
        if (personnelId == null) {
            throw new BusinessException("人员ID不能为空");
        }
        if (startDate == null || endDate == null) {
            throw new BusinessException("开始日期和结束日期不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }

        BigDecimal total = penaltyRepository.calculateTotalPenaltyByPersonnelAndDateRange(personnelId, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    private void validatePenalty(Penalty penalty) {
        if (penalty.getPersonnelId() == null) {
            throw new BusinessException("人员ID不能为空");
        }
        if (!personnelRepository.existsById(penalty.getPersonnelId())) {
            throw new BusinessException("人员不存在，ID: " + penalty.getPersonnelId());
        }
        if (penalty.getPenaltyTypeId() == null) {
            throw new BusinessException("扣罚类型ID不能为空");
        }
        if (!penaltyTypeRepository.existsById(penalty.getPenaltyTypeId())) {
            throw new BusinessException("扣罚类型不存在，ID: " + penalty.getPenaltyTypeId());
        }
        if (penalty.getAmount() == null) {
            throw new BusinessException("扣罚金额不能为空");
        }
        if (penalty.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("扣罚金额不能为负数");
        }
        if (penalty.getPenaltyDate() == null) {
            throw new BusinessException("扣罚日期不能为空");
        }
        if (!StringUtils.hasText(penalty.getReason())) {
            throw new BusinessException("扣罚原因不能为空");
        }
    }

    private String generatePenaltyNo() {
        String prefix = "PEN";
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String todayPrefix = prefix + "-" + dateStr + "-";

        List<Penalty> todayPenalties = penaltyRepository.findByPenaltyDateBetween(
                LocalDate.now(),
                LocalDate.now()
        );

        int maxSequence = 0;
        for (Penalty penalty : todayPenalties) {
            String penaltyNo = penalty.getPenaltyNo();
            if (penaltyNo != null && penaltyNo.startsWith(todayPrefix)) {
                try {
                    int seq = Integer.parseInt(penaltyNo.substring(todayPrefix.length()));
                    if (seq > maxSequence) {
                        maxSequence = seq;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }

        int nextSequence = maxSequence + 1;
        return todayPrefix + String.format("%04d", nextSequence);
    }
}
