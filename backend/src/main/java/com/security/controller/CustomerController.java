package com.security.controller;

import com.security.common.Result;
import com.security.entity.Customer;
import com.security.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/customer")
@PreAuthorize("hasRole('PROJECT_MANAGER')")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/")
    public Result<List<Customer>> list() {
        List<Customer> list = customerService.list();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<Customer> getById(@PathVariable UUID id) {
        Customer customer = customerService.getById(id);
        return Result.success(customer);
    }

    @PostMapping("/")
    public Result<Customer> save(@RequestBody Customer customer) {
        Customer saved = customerService.save(customer);
        return Result.success(saved);
    }

    @PutMapping("/")
    public Result<Customer> update(@RequestBody Customer customer) {
        Customer updated = customerService.update(customer);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable UUID id) {
        customerService.delete(id);
        return Result.success();
    }
}
