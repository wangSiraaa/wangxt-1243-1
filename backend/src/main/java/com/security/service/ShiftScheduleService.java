package com.security.service;

import com.security.common.enums.QualificationStatus;
import com.security.common.enums.ScheduleStatus;
import com.security.common.exception.BusinessException;
import com.security.entity.*;
import com.security.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShiftScheduleService {

    private static final int DEFAULT_MAX_NIGHT_SHIFTS = 3;
    private static final String CONFIG_MAX_NIGHT_SHIFTS = "max_consecutive_night_shifts";

    @Autowired
    private ShiftScheduleRepository shiftScheduleRepository;

    @Autowired
    private CustomerPointRepository customerPointRepository;

    @Autowired
    private PersonnelRepository personnelRepository;

    @Autowired
    private QualificationRepository qualificationRepository;

    @Autowired
    private QualificationTypeRepository qualificationTypeRepository;

    @Autowired
    private ShiftTemplateRepository shiftTemplateRepository;

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    public ShiftSchedule getById(UUID id) {
        if (id == null) {
            throw new BusinessException("排班ID不能为空");
        }
        return shiftScheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("排班不存在，ID: " + id));
    }

    public List<ShiftSchedule> listByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessException("开始日期和结束日期不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        return shiftScheduleRepository.findByScheduleDateBetween(startDate, endDate);
    }

    public List<ShiftSchedule> listByPersonnelAndDateRange(UUID personnelId, LocalDate startDate, LocalDate endDate) {
        if (personnelId == null) {
            throw new BusinessException("人员ID不能为空");
        }
        if (startDate == null || endDate == null) {
            throw new BusinessException("开始日期和结束日期不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        return shiftScheduleRepository.findByPersonnelIdAndScheduleDateBetween(personnelId, startDate, endDate);
    }

    public List<ShiftSchedule> listByPointAndDateRange(UUID customerPointId, LocalDate startDate, LocalDate endDate) {
        if (customerPointId == null) {
            throw new BusinessException("岗点ID不能为空");
        }
        if (startDate == null || endDate == null) {
            throw new BusinessException("开始日期和结束日期不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        return shiftScheduleRepository.findByCustomerPointIdAndScheduleDateBetween(customerPointId, startDate, endDate);
    }

    @Transactional
    public ShiftSchedule save(ShiftSchedule schedule) {
        validateShiftSchedule(schedule);
        validateKeyPositionQualification(schedule.getPersonnelId(), schedule.getCustomerPointId());
        if (schedule.getNightShift() != null && schedule.getNightShift()) {
            validateConsecutiveNightShifts(schedule.getPersonnelId(), schedule.getScheduleDate());
        }
        if (schedule.getNightShift() == null) {
            schedule.setNightShift(false);
        }
        return shiftScheduleRepository.save(schedule);
    }

    @Transactional
    public ShiftSchedule update(ShiftSchedule schedule) {
        if (schedule.getId() == null) {
            throw new BusinessException("排班ID不能为空");
        }
        ShiftSchedule existing = getById(schedule.getId());
        validateShiftSchedule(schedule);

        if (!existing.getPersonnelId().equals(schedule.getPersonnelId())
                || !existing.getCustomerPointId().equals(schedule.getCustomerPointId())) {
            validateKeyPositionQualification(schedule.getPersonnelId(), schedule.getCustomerPointId());
        }

        if ((schedule.getNightShift() != null && schedule.getNightShift())
                && (!existing.getPersonnelId().equals(schedule.getPersonnelId())
                || !existing.getScheduleDate().equals(schedule.getScheduleDate())
                || (existing.getNightShift() == null || !existing.getNightShift()))) {
            validateConsecutiveNightShifts(schedule.getPersonnelId(), schedule.getScheduleDate());
        }

        existing.setScheduleDate(schedule.getScheduleDate());
        existing.setCustomerPointId(schedule.getCustomerPointId());
        existing.setShiftTemplateId(schedule.getShiftTemplateId());
        existing.setPersonnelId(schedule.getPersonnelId());
        existing.setStatus(schedule.getStatus());
        existing.setNightShift(schedule.getNightShift());
        existing.setRemarks(schedule.getRemarks());

        return shiftScheduleRepository.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        ShiftSchedule schedule = getById(id);
        shiftScheduleRepository.delete(schedule);
    }

    @Transactional
    public ShiftSchedule checkIn(UUID id) {
        ShiftSchedule schedule = getById(id);
        if (schedule.getStatus() != ScheduleStatus.SCHEDULED) {
            throw new BusinessException("只有已排班状态可以签到");
        }
        schedule.setStatus(ScheduleStatus.CHECKED_IN);
        schedule.setCheckInTime(LocalDateTime.now());
        return shiftScheduleRepository.save(schedule);
    }

    @Transactional
    public ShiftSchedule checkOut(UUID id) {
        ShiftSchedule schedule = getById(id);
        if (schedule.getStatus() != ScheduleStatus.CHECKED_IN) {
            throw new BusinessException("只有已签到状态可以签退");
        }
        schedule.setStatus(ScheduleStatus.CHECKED_OUT);
        schedule.setCheckOutTime(LocalDateTime.now());
        return shiftScheduleRepository.save(schedule);
    }

    @Transactional
    public List<ShiftSchedule> batchSave(List<ShiftSchedule> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            throw new BusinessException("排班列表不能为空");
        }
        List<ShiftSchedule> savedSchedules = new ArrayList<>();
        for (ShiftSchedule schedule : schedules) {
            savedSchedules.add(save(schedule));
        }
        return savedSchedules;
    }

    public Map<LocalDate, List<ShiftSchedule>> getCalendarView(LocalDate startDate, LocalDate endDate) {
        List<ShiftSchedule> schedules = listByDateRange(startDate, endDate);
        return schedules.stream()
                .collect(Collectors.groupingBy(ShiftSchedule::getScheduleDate, TreeMap::new, Collectors.toList()));
    }

    public void validateKeyPositionQualification(UUID personnelId, UUID customerPointId) {
        CustomerPoint customerPoint = customerPointRepository.findById(customerPointId)
                .orElseThrow(() -> new BusinessException("岗点不存在，ID: " + customerPointId));

        if (customerPoint.getKeyPosition() == null || !customerPoint.getKeyPosition()) {
            return;
        }

        List<QualificationType> requiredTypes = qualificationTypeRepository.findByRequiredForKeyPositionTrue();
        if (requiredTypes.isEmpty()) {
            return;
        }

        List<Qualification> personnelQualifications = qualificationRepository.findByPersonnelId(personnelId);

        for (QualificationType requiredType : requiredTypes) {
            boolean hasValidQualification = personnelQualifications.stream()
                    .anyMatch(q -> q.getQualificationTypeId().equals(requiredType.getId())
                            && q.getStatus() == QualificationStatus.VALID
                            && q.getExpiryDate().isAfter(LocalDate.now()));

            if (!hasValidQualification) {
                throw new BusinessException("该人员证件已过期，不能安排到重点岗位");
            }
        }
    }

    public int countConsecutiveNightShifts(UUID personnelId, LocalDate scheduleDate) {
        List<ShiftSchedule> nightShifts = shiftScheduleRepository.findByNightShiftTrueAndPersonnelId(personnelId);
        if (nightShifts.isEmpty()) {
            return 1;
        }

        Set<LocalDate> nightShiftDates = nightShifts.stream()
                .map(ShiftSchedule::getScheduleDate)
                .collect(Collectors.toSet());
        nightShiftDates.add(scheduleDate);

        int count = 0;
        LocalDate currentDate = scheduleDate;
        while (nightShiftDates.contains(currentDate)) {
            count++;
            currentDate = currentDate.minusDays(1);
        }

        currentDate = scheduleDate.plusDays(1);
        while (nightShiftDates.contains(currentDate)) {
            count++;
            currentDate = currentDate.plusDays(1);
        }

        return count;
    }

    public boolean isNightShift(ShiftTemplate template) {
        if (template == null) {
            return false;
        }
        return template.getShiftType() != null && template.getShiftType().name().equals("NIGHT");
    }

    private void validateShiftSchedule(ShiftSchedule schedule) {
        if (schedule == null) {
            throw new BusinessException("排班信息不能为空");
        }
        if (schedule.getScheduleDate() == null) {
            throw new BusinessException("排班日期不能为空");
        }
        if (schedule.getCustomerPointId() == null) {
            throw new BusinessException("岗点ID不能为空");
        }
        if (schedule.getShiftTemplateId() == null) {
            throw new BusinessException("班次模板ID不能为空");
        }
        if (schedule.getPersonnelId() == null) {
            throw new BusinessException("人员ID不能为空");
        }

        if (!customerPointRepository.existsById(schedule.getCustomerPointId())) {
            throw new BusinessException("岗点不存在，ID: " + schedule.getCustomerPointId());
        }
        if (!shiftTemplateRepository.existsById(schedule.getShiftTemplateId())) {
            throw new BusinessException("班次模板不存在，ID: " + schedule.getShiftTemplateId());
        }
        if (!personnelRepository.existsById(schedule.getPersonnelId())) {
            throw new BusinessException("人员不存在，ID: " + schedule.getPersonnelId());
        }
    }

    private void validateConsecutiveNightShifts(UUID personnelId, LocalDate scheduleDate) {
        int maxAllowed = getMaxConsecutiveNightShifts(personnelId);
        int consecutiveCount = countConsecutiveNightShifts(personnelId, scheduleDate);
        if (consecutiveCount > maxAllowed) {
            throw new BusinessException("该人员连续夜班已超过" + maxAllowed + "天，请调整排班");
        }
    }

    private int getMaxConsecutiveNightShifts(UUID personnelId) {
        Personnel personnel = personnelRepository.findById(personnelId).orElse(null);
        if (personnel != null && personnel.getMaxConsecutiveNightShifts() != null) {
            return personnel.getMaxConsecutiveNightShifts();
        }

        SystemConfig config = systemConfigRepository.findByConfigKey(CONFIG_MAX_NIGHT_SHIFTS);
        if (config != null && config.getConfigValue() != null) {
            try {
                return Integer.parseInt(config.getConfigValue());
            } catch (NumberFormatException e) {
                return DEFAULT_MAX_NIGHT_SHIFTS;
            }
        }

        return DEFAULT_MAX_NIGHT_SHIFTS;
    }
}
