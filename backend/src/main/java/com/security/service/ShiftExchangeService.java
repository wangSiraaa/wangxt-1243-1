package com.security.service;

import com.security.common.enums.ExchangeStatus;
import com.security.common.exception.BusinessException;
import com.security.entity.Personnel;
import com.security.entity.ShiftExchange;
import com.security.entity.ShiftSchedule;
import com.security.repository.PersonnelRepository;
import com.security.repository.ShiftExchangeRepository;
import com.security.repository.ShiftScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ShiftExchangeService {

    @Autowired
    private ShiftExchangeRepository shiftExchangeRepository;

    @Autowired
    private ShiftScheduleRepository shiftScheduleRepository;

    @Autowired
    private PersonnelRepository personnelRepository;

    @Autowired
    private ShiftScheduleService shiftScheduleService;

    @Transactional
    public ShiftExchange createExchange(ShiftExchange exchange) {
        validateShiftExchange(exchange);

        ShiftSchedule originalSchedule = shiftScheduleRepository.findById(exchange.getOriginalScheduleId())
                .orElseThrow(() -> new BusinessException("原排班不存在，ID: " + exchange.getOriginalScheduleId()));

        if (!originalSchedule.getPersonnelId().equals(exchange.getRequesterId())) {
            throw new BusinessException("只有原排班人员才能申请换班");
        }

        if (!personnelRepository.existsById(exchange.getReplacementId())) {
            throw new BusinessException("替班人员不存在，ID: " + exchange.getReplacementId());
        }

        if (exchange.getRequesterId().equals(exchange.getReplacementId())) {
            throw new BusinessException("申请人和替班人不能是同一人");
        }

        ShiftExchange existingExchange = shiftExchangeRepository.findByOriginalScheduleId(exchange.getOriginalScheduleId());
        if (existingExchange != null && existingExchange.getStatus() == ExchangeStatus.PENDING) {
            throw new BusinessException("该排班已有待审批的换班申请");
        }

        shiftScheduleService.validateKeyPositionQualification(
                exchange.getReplacementId(), originalSchedule.getCustomerPointId());

        if (originalSchedule.getNightShift() != null && originalSchedule.getNightShift()) {
            int consecutiveCount = shiftScheduleService.countConsecutiveNightShifts(
                    exchange.getReplacementId(), originalSchedule.getScheduleDate());
            Personnel personnel = personnelRepository.findById(exchange.getReplacementId()).orElse(null);
            int maxAllowed = (personnel != null && personnel.getMaxConsecutiveNightShifts() != null)
                    ? personnel.getMaxConsecutiveNightShifts() : 3;
            if (consecutiveCount > maxAllowed) {
                throw new BusinessException("替班人员连续夜班已超过" + maxAllowed + "天，请调整换班");
            }
        }

        exchange.setStatus(ExchangeStatus.PENDING);
        exchange.setApproverId(null);
        exchange.setApprovalRemarks(null);
        exchange.setApprovedAt(null);

        return shiftExchangeRepository.save(exchange);
    }

    @Transactional
    public ShiftExchange approveExchange(UUID exchangeId, UUID approverId, String remarks) {
        if (exchangeId == null) {
            throw new BusinessException("换班申请ID不能为空");
        }
        if (approverId == null) {
            throw new BusinessException("审批人ID不能为空");
        }

        ShiftExchange exchange = shiftExchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new BusinessException("换班申请不存在，ID: " + exchangeId));

        if (exchange.getStatus() != ExchangeStatus.PENDING) {
            throw new BusinessException("该换班申请不是待审批状态，无法审批");
        }

        exchange.setStatus(ExchangeStatus.APPROVED);
        exchange.setApproverId(approverId);
        exchange.setApprovalRemarks(remarks);
        exchange.setApprovedAt(LocalDateTime.now());

        ShiftSchedule originalSchedule = shiftScheduleRepository.findById(exchange.getOriginalScheduleId())
                .orElseThrow(() -> new BusinessException("原排班不存在，ID: " + exchange.getOriginalScheduleId()));
        originalSchedule.setPersonnelId(exchange.getReplacementId());
        shiftScheduleRepository.save(originalSchedule);

        return shiftExchangeRepository.save(exchange);
    }

    @Transactional
    public ShiftExchange rejectExchange(UUID exchangeId, UUID approverId, String remarks) {
        if (exchangeId == null) {
            throw new BusinessException("换班申请ID不能为空");
        }
        if (approverId == null) {
            throw new BusinessException("审批人ID不能为空");
        }
        if (remarks == null || remarks.trim().isEmpty()) {
            throw new BusinessException("驳回原因不能为空");
        }

        ShiftExchange exchange = shiftExchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new BusinessException("换班申请不存在，ID: " + exchangeId));

        if (exchange.getStatus() != ExchangeStatus.PENDING) {
            throw new BusinessException("该换班申请不是待审批状态，无法驳回");
        }

        exchange.setStatus(ExchangeStatus.REJECTED);
        exchange.setApproverId(approverId);
        exchange.setApprovalRemarks(remarks);
        exchange.setApprovedAt(LocalDateTime.now());

        return shiftExchangeRepository.save(exchange);
    }

    public List<ShiftExchange> listPendingExchanges() {
        return shiftExchangeRepository.findByStatus(ExchangeStatus.PENDING);
    }

    public ShiftExchange getById(UUID id) {
        if (id == null) {
            throw new BusinessException("换班申请ID不能为空");
        }
        return shiftExchangeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("换班申请不存在，ID: " + id));
    }

    public List<ShiftExchange> listAll() {
        return shiftExchangeRepository.findAll();
    }

    public List<ShiftExchange> listByRequester(UUID requesterId) {
        if (requesterId == null) {
            throw new BusinessException("申请人ID不能为空");
        }
        return shiftExchangeRepository.findByRequesterId(requesterId);
    }

    private void validateShiftExchange(ShiftExchange exchange) {
        if (exchange == null) {
            throw new BusinessException("换班申请信息不能为空");
        }
        if (exchange.getOriginalScheduleId() == null) {
            throw new BusinessException("原排班ID不能为空");
        }
        if (exchange.getRequesterId() == null) {
            throw new BusinessException("申请人ID不能为空");
        }
        if (exchange.getReplacementId() == null) {
            throw new BusinessException("替班人ID不能为空");
        }
    }
}
