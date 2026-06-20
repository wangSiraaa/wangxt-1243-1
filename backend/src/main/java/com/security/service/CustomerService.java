package com.security.service;

import com.security.common.exception.BusinessException;
import com.security.entity.Customer;
import com.security.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public Customer getById(UUID id) {
        if (id == null) {
            throw new BusinessException("客户ID不能为空");
        }
        return customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("客户不存在，ID: " + id));
    }

    public List<Customer> list() {
        return customerRepository.findAll();
    }

    public Customer save(Customer customer) {
        validateCustomer(customer);
        
        if (customer.getEnabled() == null) {
            customer.setEnabled(true);
        }
        
        return customerRepository.save(customer);
    }

    public Customer update(Customer customer) {
        if (customer.getId() == null) {
            throw new BusinessException("客户ID不能为空");
        }
        
        Customer existing = getById(customer.getId());
        validateCustomer(customer);
        
        existing.setName(customer.getName());
        existing.setContactPerson(customer.getContactPerson());
        existing.setContactPhone(customer.getContactPhone());
        existing.setAddress(customer.getAddress());
        existing.setEnabled(customer.getEnabled());
        
        return customerRepository.save(existing);
    }

    public void delete(UUID id) {
        Customer customer = getById(id);
        customerRepository.delete(customer);
    }

    private void validateCustomer(Customer customer) {
        if (!StringUtils.hasText(customer.getName())) {
            throw new BusinessException("客户名称不能为空");
        }
        if (customer.getName().length() > 100) {
            throw new BusinessException("客户名称长度不能超过100");
        }
        if (customer.getContactPerson() != null && customer.getContactPerson().length() > 50) {
            throw new BusinessException("联系人长度不能超过50");
        }
        if (customer.getContactPhone() != null && customer.getContactPhone().length() > 20) {
            throw new BusinessException("联系电话长度不能超过20");
        }
        if (customer.getAddress() != null && customer.getAddress().length() > 255) {
            throw new BusinessException("地址长度不能超过255");
        }
    }
}
