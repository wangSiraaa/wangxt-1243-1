package com.security.service;

import com.security.common.exception.BusinessException;
import com.security.entity.CustomerPoint;
import com.security.repository.CustomerPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerPointService {

    @Autowired
    private CustomerPointRepository customerPointRepository;

    public CustomerPoint getById(UUID id) {
        if (id == null) {
            throw new BusinessException("点位ID不能为空");
        }
        return customerPointRepository.findById(id)
                .orElseThrow(() -> new BusinessException("点位不存在，ID: " + id));
    }

    public List<CustomerPoint> list() {
        return customerPointRepository.findAll();
    }

    public List<CustomerPoint> listByCustomerId(UUID customerId) {
        if (customerId == null) {
            throw new BusinessException("客户ID不能为空");
        }
        return customerPointRepository.findByCustomerId(customerId);
    }

    public List<CustomerPoint> listKeyPositions() {
        return customerPointRepository.findByKeyPositionTrue();
    }

    public CustomerPoint save(CustomerPoint customerPoint) {
        validateCustomerPoint(customerPoint);
        
        if (customerPoint.getPointCode() != null) {
            List<CustomerPoint> existing = customerPointRepository.findAll();
            for (CustomerPoint point : existing) {
                if (customerPoint.getPointCode().equals(point.getPointCode())) {
                    throw new BusinessException("点位编码已存在: " + customerPoint.getPointCode());
                }
            }
        }
        
        if (customerPoint.getKeyPosition() == null) {
            customerPoint.setKeyPosition(false);
        }
        if (customerPoint.getEnabled() == null) {
            customerPoint.setEnabled(true);
        }
        
        return customerPointRepository.save(customerPoint);
    }

    public CustomerPoint update(CustomerPoint customerPoint) {
        if (customerPoint.getId() == null) {
            throw new BusinessException("点位ID不能为空");
        }
        
        CustomerPoint existing = getById(customerPoint.getId());
        validateCustomerPoint(customerPoint);
        
        if (customerPoint.getPointCode() != null) {
            List<CustomerPoint> allPoints = customerPointRepository.findAll();
            for (CustomerPoint point : allPoints) {
                if (customerPoint.getPointCode().equals(point.getPointCode()) 
                        && !point.getId().equals(customerPoint.getId())) {
                    throw new BusinessException("点位编码已存在: " + customerPoint.getPointCode());
                }
            }
        }
        
        existing.setCustomerId(customerPoint.getCustomerId());
        existing.setPointName(customerPoint.getPointName());
        existing.setPointCode(customerPoint.getPointCode());
        existing.setAddress(customerPoint.getAddress());
        existing.setKeyPosition(customerPoint.getKeyPosition());
        existing.setDescription(customerPoint.getDescription());
        existing.setEnabled(customerPoint.getEnabled());
        
        return customerPointRepository.save(existing);
    }

    public void delete(UUID id) {
        CustomerPoint customerPoint = getById(id);
        customerPointRepository.delete(customerPoint);
    }

    public CustomerPoint toggleEnabled(UUID id) {
        CustomerPoint customerPoint = getById(id);
        customerPoint.setEnabled(!customerPoint.getEnabled());
        return customerPointRepository.save(customerPoint);
    }

    public CustomerPoint setKeyPosition(UUID id, boolean keyPosition) {
        CustomerPoint customerPoint = getById(id);
        customerPoint.setKeyPosition(keyPosition);
        return customerPointRepository.save(customerPoint);
    }

    private void validateCustomerPoint(CustomerPoint customerPoint) {
        if (customerPoint.getCustomerId() == null) {
            throw new BusinessException("客户ID不能为空");
        }
        if (!StringUtils.hasText(customerPoint.getPointName())) {
            throw new BusinessException("点位名称不能为空");
        }
        if (customerPoint.getPointName().length() > 100) {
            throw new BusinessException("点位名称长度不能超过100");
        }
        if (customerPoint.getPointCode() != null && customerPoint.getPointCode().length() > 50) {
            throw new BusinessException("点位编码长度不能超过50");
        }
        if (customerPoint.getAddress() != null && customerPoint.getAddress().length() > 255) {
            throw new BusinessException("地址长度不能超过255");
        }
    }
}
