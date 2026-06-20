package com.security.service;

import com.security.common.enums.ScheduleStatus;
import com.security.common.enums.SettlementStatus;
import com.security.common.exception.BusinessException;
import com.security.entity.Customer;
import com.security.entity.Settlement;
import com.security.entity.SettlementDetail;
import com.security.entity.SystemConfig;
import com.security.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SettlementService {

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private SettlementDetailRepository settlementDetailRepository;

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ShiftScheduleRepository shiftScheduleRepository;

    @Autowired
    private PatrolEventRepository patrolEventRepository;

    @Autowired
    private PenaltyRepository penaltyRepository;

    private static final String SHIFT_UNIT_PRICE_KEY = "shift_unit_price";
    private static final String SETTLEMENT_NO_PREFIX = "SET";

    public Settlement getById(UUID id) {
        if (id == null) {
            throw new BusinessException("结算单ID不能为空");
        }
        return settlementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("结算单不存在，ID: " + id));
    }

    public List<Settlement> listByCustomer(UUID customerId) {
        if (customerId == null) {
            throw new BusinessException("客户ID不能为空");
        }
        return settlementRepository.findByCustomerId(customerId);
    }

    public Settlement generateSettlement(UUID customerId, String settlementMonth) {
        if (customerId == null) {
            throw new BusinessException("客户ID不能为空");
        }
        if (settlementMonth == null || settlementMonth.length() != 7) {
            throw new BusinessException("结算月份格式不正确，应为yyyy-MM");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException("客户不存在，ID: " + customerId));

        Settlement existingSettlement = settlementRepository.findByCustomerIdAndSettlementMonth(customerId, settlementMonth);
        if (existingSettlement != null) {
            throw new BusinessException("该客户该月份已存在结算单: " + settlementMonth);
        }

        YearMonth yearMonth = YearMonth.parse(settlementMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Long totalShifts = shiftScheduleRepository.countByCustomerIdAndScheduleDateBetweenAndStatus(
                customerId, startDate, endDate, ScheduleStatus.CHECKED_OUT);
        if (totalShifts == null) {
            totalShifts = 0L;
        }

        SystemConfig priceConfig = systemConfigRepository.findByConfigKey(SHIFT_UNIT_PRICE_KEY);
        if (priceConfig == null || priceConfig.getConfigValue() == null) {
            throw new BusinessException("系统参数未配置: " + SHIFT_UNIT_PRICE_KEY);
        }
        BigDecimal shiftUnitPrice;
        try {
            shiftUnitPrice = new BigDecimal(priceConfig.getConfigValue());
        } catch (NumberFormatException e) {
            throw new BusinessException("班次单价配置格式不正确: " + priceConfig.getConfigValue());
        }

        BigDecimal totalAmount = shiftUnitPrice.multiply(BigDecimal.valueOf(totalShifts));

        BigDecimal penaltyAmount = penaltyRepository.calculateTotalPenaltyByCustomerAndDateRange(
                customerId, startDate, endDate);
        if (penaltyAmount == null) {
            penaltyAmount = BigDecimal.ZERO;
        }

        BigDecimal actualAmount = totalAmount.subtract(penaltyAmount);

        Long unconfirmedEventCount = patrolEventRepository.countUnconfirmedByCustomerId(customerId);
        if (unconfirmedEventCount == null) {
            unconfirmedEventCount = 0L;
        }

        Settlement settlement = new Settlement();
        settlement.setCustomerId(customerId);
        settlement.setSettlementMonth(settlementMonth);
        settlement.setTotalShifts(totalShifts.intValue());
        settlement.setTotalAmount(totalAmount);
        settlement.setPenaltyAmount(penaltyAmount);
        settlement.setActualAmount(actualAmount);
        settlement.setUnconfirmedEventCount(unconfirmedEventCount.intValue());

        String settlementNo = generateSettlementNo();
        settlement.setSettlementNo(settlementNo);

        if (unconfirmedEventCount > 0) {
            settlement.setStatus(SettlementStatus.DRAFT);
            settlementRepository.save(settlement);
            throw new BusinessException("存在 " + unconfirmedEventCount + " 条未确认的异常事件，结算单已保存为草稿状态，请先确认所有异常事件后再确认结算单");
        } else {
            settlement.setStatus(SettlementStatus.DRAFT);
        }

        return settlementRepository.save(settlement);
    }

    public Settlement confirmSettlement(UUID settlementId) {
        if (settlementId == null) {
            throw new BusinessException("结算单ID不能为空");
        }

        Settlement settlement = getById(settlementId);

        if (settlement.getStatus() != SettlementStatus.DRAFT) {
            throw new BusinessException("只有草稿状态的结算单可以确认");
        }

        if (settlement.getUnconfirmedEventCount() != null && settlement.getUnconfirmedEventCount() > 0) {
            throw new BusinessException("存在 " + settlement.getUnconfirmedEventCount() + " 条未确认的异常事件，请先确认所有异常事件后再确认结算单");
        }

        Long currentUnconfirmedCount = patrolEventRepository.countUnconfirmedByCustomerId(settlement.getCustomerId());
        if (currentUnconfirmedCount != null && currentUnconfirmedCount > 0) {
            settlement.setUnconfirmedEventCount(currentUnconfirmedCount.intValue());
            settlementRepository.save(settlement);
            throw new BusinessException("存在 " + currentUnconfirmedCount + " 条未确认的异常事件，请先确认所有异常事件后再确认结算单");
        }

        settlement.setStatus(SettlementStatus.CONFIRMED);
        return settlementRepository.save(settlement);
    }

    public Settlement updateStatus(UUID id, SettlementStatus status) {
        if (status == null) {
            throw new BusinessException("结算状态不能为空");
        }
        Settlement settlement = getById(id);
        settlement.setStatus(status);
        return settlementRepository.save(settlement);
    }

    public List<Settlement> listByStatus(SettlementStatus status) {
        if (status == null) {
            throw new BusinessException("结算状态不能为空");
        }
        return settlementRepository.findByStatus(status);
    }

    public List<Settlement> list(UUID customerId, SettlementStatus status) {
        List<Settlement> settlements = settlementRepository.findAll();
        
        if (customerId != null) {
            settlements = settlements.stream()
                    .filter(s -> s.getCustomerId().equals(customerId))
                    .collect(Collectors.toList());
        }
        
        if (status != null) {
            settlements = settlements.stream()
                    .filter(s -> s.getStatus() == status)
                    .collect(Collectors.toList());
        }
        
        return settlements;
    }

    public List<Settlement> listByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessException("开始日期和结束日期不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }

        return settlementRepository.findAll().stream()
                .filter(s -> {
                    YearMonth ym = YearMonth.parse(s.getSettlementMonth());
                    LocalDate settlementDate = ym.atDay(1);
                    return !settlementDate.isBefore(startDate) && !settlementDate.isAfter(endDate);
                })
                .toList();
    }

    public List<SettlementDetail> getSettlementDetails(UUID settlementId) {
        if (settlementId == null) {
            throw new BusinessException("结算单ID不能为空");
        }
        getById(settlementId);
        return settlementDetailRepository.findBySettlementId(settlementId);
    }

    private String generateSettlementNo() {
        String prefix = SETTLEMENT_NO_PREFIX;
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String todayPrefix = prefix + "-" + dateStr + "-";

        List<Settlement> todaySettlements = settlementRepository.findAll().stream()
                .filter(s -> s.getCreatedAt() != null && 
                        s.getCreatedAt().toLocalDate().isEqual(LocalDate.now()))
                .toList();

        int maxSequence = 0;
        for (Settlement settlement : todaySettlements) {
            String settlementNo = settlement.getSettlementNo();
            if (settlementNo != null && settlementNo.startsWith(todayPrefix)) {
                try {
                    int seq = Integer.parseInt(settlementNo.substring(todayPrefix.length()));
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
