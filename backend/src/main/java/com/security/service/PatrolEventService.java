package com.security.service;

import com.security.common.enums.EventStatus;
import com.security.common.enums.UserRole;
import com.security.common.exception.BusinessException;
import com.security.entity.CustomerPoint;
import com.security.entity.PatrolEvent;
import com.security.entity.User;
import com.security.repository.CustomerPointRepository;
import com.security.repository.PatrolEventRepository;
import com.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PatrolEventService {

    @Autowired
    private PatrolEventRepository patrolEventRepository;

    @Autowired
    private CustomerPointRepository customerPointRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShiftScheduleRepository shiftScheduleRepository;

    @Autowired
    private ShiftExchangeRepository shiftExchangeRepository;

    public PatrolEvent getById(UUID id) {
        if (id == null) {
            throw new BusinessException("巡更事件ID不能为空");
        }
        PatrolEvent event = patrolEventRepository.findById(id)
                .orElseThrow(() -> new BusinessException("巡更事件不存在，ID: " + id));
        loadExchangeInfo(event);
        return event;
    }

    private void loadExchangeInfo(PatrolEvent event) {
        if (event.getScheduleId() == null) {
            return;
        }
        shiftScheduleRepository.findById(event.getScheduleId()).ifPresent(schedule -> {
            if (schedule.getExchangeId() != null) {
                shiftExchangeRepository.findById(schedule.getExchangeId()).ifPresent(event::setExchangeInfo);
            }
        });
    }

    public List<PatrolEvent> listByCustomerPoint(UUID customerPointId) {
        if (customerPointId == null) {
            throw new BusinessException("客户点位ID不能为空");
        }
        return patrolEventRepository.findByCustomerPointId(customerPointId);
    }

    public List<PatrolEvent> listUnconfirmed() {
        return patrolEventRepository.findByCustomerConfirmedFalse();
    }

    public List<PatrolEvent> listByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BusinessException("开始时间和结束时间不能为空");
        }
        if (startTime.isAfter(endTime)) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }
        return patrolEventRepository.findByEventTimeBetween(startTime, endTime);
    }

    public List<PatrolEvent> listByStatus(EventStatus status) {
        if (status == null) {
            throw new BusinessException("事件状态不能为空");
        }
        return patrolEventRepository.findByStatus(status);
    }

    public List<PatrolEvent> list(UUID pointId, EventStatus status, LocalDateTime startTime, LocalDateTime endTime) {
        List<PatrolEvent> events = patrolEventRepository.findAll();
        
        if (pointId != null) {
            events = events.stream()
                    .filter(e -> e.getCustomerPointId().equals(pointId))
                    .collect(Collectors.toList());
        }
        
        if (status != null) {
            events = events.stream()
                    .filter(e -> e.getStatus() == status)
                    .collect(Collectors.toList());
        }
        
        if (startTime != null) {
            events = events.stream()
                    .filter(e -> !e.getEventTime().isBefore(startTime))
                    .collect(Collectors.toList());
        }
        
        if (endTime != null) {
            events = events.stream()
                    .filter(e -> !e.getEventTime().isAfter(endTime))
                    .collect(Collectors.toList());
        }
        
        return events;
    }

    public PatrolEvent save(PatrolEvent event) {
        validatePatrolEvent(event);

        if (event.getCustomerConfirmed() == null) {
            event.setCustomerConfirmed(false);
        }
        if (event.getStatus() == null) {
            event.setStatus(EventStatus.OPEN);
        }

        String eventNo = generateEventNo();
        event.setEventNo(eventNo);

        return patrolEventRepository.save(event);
    }

    public PatrolEvent update(PatrolEvent event) {
        if (event.getId() == null) {
            throw new BusinessException("巡更事件ID不能为空");
        }

        PatrolEvent existing = getById(event.getId());
        validatePatrolEvent(event);

        existing.setEventType(event.getEventType());
        existing.setEventLevel(event.getEventLevel());
        existing.setDescription(event.getDescription());
        existing.setEventTime(event.getEventTime());
        existing.setStatus(event.getStatus());

        return patrolEventRepository.save(existing);
    }

    public PatrolEvent updateStatus(UUID id, EventStatus status) {
        if (status == null) {
            throw new BusinessException("事件状态不能为空");
        }
        PatrolEvent event = getById(id);
        event.setStatus(status);
        return patrolEventRepository.save(event);
    }

    public PatrolEvent confirmByCustomer(UUID eventId, UUID customerUserId, String remarks) {
        if (eventId == null) {
            throw new BusinessException("巡更事件ID不能为空");
        }
        if (customerUserId == null) {
            throw new BusinessException("客户用户ID不能为空");
        }

        PatrolEvent event = getById(eventId);

        if (event.getCustomerConfirmed()) {
            throw new BusinessException("该事件已被客户确认");
        }

        User customerUser = userRepository.findById(customerUserId)
                .orElseThrow(() -> new BusinessException("客户用户不存在，ID: " + customerUserId));

        if (customerUser.getRole() != UserRole.CUSTOMER) {
            throw new BusinessException("只有客户角色用户可以确认异常事件");
        }

        CustomerPoint customerPoint = customerPointRepository.findById(event.getCustomerPointId())
                .orElseThrow(() -> new BusinessException("客户点位不存在，ID: " + event.getCustomerPointId()));

        if (customerUser.getCustomerId() == null || !customerUser.getCustomerId().equals(customerPoint.getCustomerId())) {
            throw new BusinessException("该用户不属于该事件所属客户，无法确认");
        }

        event.setCustomerConfirmed(true);
        event.setCustomerConfirmerId(customerUserId);
        event.setCustomerConfirmedAt(LocalDateTime.now());
        event.setCustomerRemarks(remarks);

        return patrolEventRepository.save(event);
    }

    public Long countUnconfirmedByCustomer(UUID customerId) {
        if (customerId == null) {
            throw new BusinessException("客户ID不能为空");
        }
        Long count = patrolEventRepository.countUnconfirmedByCustomerId(customerId);
        return count != null ? count : 0L;
    }

    private void validatePatrolEvent(PatrolEvent event) {
        if (event.getCustomerPointId() == null) {
            throw new BusinessException("客户点位ID不能为空");
        }
        if (event.getReporterId() == null) {
            throw new BusinessException("上报人ID不能为空");
        }
        if (!StringUtils.hasText(event.getEventType())) {
            throw new BusinessException("事件类型不能为空");
        }
        if (event.getEventType().length() > 50) {
            throw new BusinessException("事件类型长度不能超过50");
        }
        if (event.getEventLevel() == null) {
            throw new BusinessException("事件级别不能为空");
        }
        if (!StringUtils.hasText(event.getDescription())) {
            throw new BusinessException("事件描述不能为空");
        }
        if (event.getEventTime() == null) {
            throw new BusinessException("事件时间不能为空");
        }
    }

    private String generateEventNo() {
        String prefix = "EVT";
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String todayPrefix = prefix + "-" + dateStr + "-";

        List<PatrolEvent> todayEvents = patrolEventRepository.findByEventTimeBetween(
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atTime(23, 59, 59)
        );

        int maxSequence = 0;
        for (PatrolEvent event : todayEvents) {
            String eventNo = event.getEventNo();
            if (eventNo != null && eventNo.startsWith(todayPrefix)) {
                try {
                    int seq = Integer.parseInt(eventNo.substring(todayPrefix.length()));
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
