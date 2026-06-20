package com.security.service;

import com.security.common.enums.PersonnelStatus;
import com.security.common.exception.BusinessException;
import com.security.entity.Personnel;
import com.security.repository.PersonnelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class PersonnelService {

    @Autowired
    private PersonnelRepository personnelRepository;

    public Personnel getById(UUID id) {
        if (id == null) {
            throw new BusinessException("人员ID不能为空");
        }
        return personnelRepository.findById(id)
                .orElseThrow(() -> new BusinessException("人员不存在，ID: " + id));
    }

    public List<Personnel> list() {
        return personnelRepository.findAll();
    }

    public List<Personnel> listByStatus(PersonnelStatus status) {
        if (status == null) {
            throw new BusinessException("状态不能为空");
        }
        return personnelRepository.findByStatus(status);
    }

    public Personnel save(Personnel personnel) {
        validatePersonnel(personnel);
        
        Personnel existing = personnelRepository.findByEmployeeNo(personnel.getEmployeeNo());
        if (existing != null) {
            throw new BusinessException("员工编号已存在: " + personnel.getEmployeeNo());
        }
        
        if (personnel.getStatus() == null) {
            personnel.setStatus(PersonnelStatus.ACTIVE);
        }
        if (personnel.getMaxConsecutiveNightShifts() == null) {
            personnel.setMaxConsecutiveNightShifts(3);
        }
        
        return personnelRepository.save(personnel);
    }

    public Personnel update(Personnel personnel) {
        if (personnel.getId() == null) {
            throw new BusinessException("人员ID不能为空");
        }
        
        Personnel existing = getById(personnel.getId());
        validatePersonnel(personnel);
        
        Personnel existingByNo = personnelRepository.findByEmployeeNo(personnel.getEmployeeNo());
        if (existingByNo != null && !existingByNo.getId().equals(personnel.getId())) {
            throw new BusinessException("员工编号已存在: " + personnel.getEmployeeNo());
        }
        
        existing.setEmployeeNo(personnel.getEmployeeNo());
        existing.setName(personnel.getName());
        existing.setGender(personnel.getGender());
        existing.setPhone(personnel.getPhone());
        existing.setIdCard(personnel.getIdCard());
        existing.setAddress(personnel.getAddress());
        existing.setStatus(personnel.getStatus());
        existing.setMaxConsecutiveNightShifts(personnel.getMaxConsecutiveNightShifts());
        
        return personnelRepository.save(existing);
    }

    public void delete(UUID id) {
        Personnel personnel = getById(id);
        personnelRepository.delete(personnel);
    }

    public Personnel updateStatus(UUID id, PersonnelStatus status) {
        if (status == null) {
            throw new BusinessException("状态不能为空");
        }
        Personnel personnel = getById(id);
        personnel.setStatus(status);
        return personnelRepository.save(personnel);
    }

    private void validatePersonnel(Personnel personnel) {
        if (!StringUtils.hasText(personnel.getEmployeeNo())) {
            throw new BusinessException("员工编号不能为空");
        }
        if (personnel.getEmployeeNo().length() > 50) {
            throw new BusinessException("员工编号长度不能超过50");
        }
        if (!StringUtils.hasText(personnel.getName())) {
            throw new BusinessException("姓名不能为空");
        }
        if (personnel.getName().length() > 50) {
            throw new BusinessException("姓名长度不能超过50");
        }
        if (personnel.getPhone() != null && personnel.getPhone().length() > 20) {
            throw new BusinessException("电话长度不能超过20");
        }
        if (personnel.getIdCard() != null && personnel.getIdCard().length() > 18) {
            throw new BusinessException("身份证号长度不能超过18");
        }
        if (personnel.getMaxConsecutiveNightShifts() != null && personnel.getMaxConsecutiveNightShifts() < 0) {
            throw new BusinessException("最大连续夜班数不能为负数");
        }
    }
}
