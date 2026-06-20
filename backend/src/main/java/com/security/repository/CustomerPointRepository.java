package com.security.repository;

import com.security.entity.CustomerPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerPointRepository extends JpaRepository<CustomerPoint, UUID> {

    List<CustomerPoint> findByCustomerId(UUID customerId);

    List<CustomerPoint> findByKeyPositionTrue();
}
