package com.security.controller;

import com.security.common.Result;
import com.security.entity.CustomerPoint;
import com.security.service.CustomerPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/customer-point")
@PreAuthorize("isAuthenticated()")
public class CustomerPointController {

    @Autowired
    private CustomerPointService customerPointService;

    @GetMapping("/")
    public Result<List<CustomerPoint>> list() {
        List<CustomerPoint> list = customerPointService.list();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<CustomerPoint> getById(@PathVariable UUID id) {
        CustomerPoint customerPoint = customerPointService.getById(id);
        return Result.success(customerPoint);
    }

    @GetMapping("/customer/{customerId}")
    public Result<List<CustomerPoint>> listByCustomerId(@PathVariable UUID customerId) {
        List<CustomerPoint> list = customerPointService.listByCustomerId(customerId);
        return Result.success(list);
    }

    @GetMapping("/key-positions")
    public Result<List<CustomerPoint>> listKeyPositions() {
        List<CustomerPoint> list = customerPointService.listKeyPositions();
        return Result.success(list);
    }

    @PostMapping("/")
    public Result<CustomerPoint> save(@RequestBody CustomerPoint customerPoint) {
        CustomerPoint saved = customerPointService.save(customerPoint);
        return Result.success(saved);
    }

    @PutMapping("/")
    public Result<CustomerPoint> update(@RequestBody CustomerPoint customerPoint) {
        CustomerPoint updated = customerPointService.update(customerPoint);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable UUID id) {
        customerPointService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/key-position")
    public Result<CustomerPoint> setKeyPosition(@PathVariable UUID id, @RequestParam boolean keyPosition) {
        CustomerPoint updated = customerPointService.setKeyPosition(id, keyPosition);
        return Result.success(updated);
    }
}
