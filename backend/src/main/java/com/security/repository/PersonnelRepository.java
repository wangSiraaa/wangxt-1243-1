package com.security.repository;

import com.security.common.enums.PersonnelStatus;
import com.security.entity.Personnel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PersonnelRepository extends JpaRepository<Personnel, UUID> {

    Personnel findByEmployeeNo(String employeeNo);

    List<Personnel> findByStatus(PersonnelStatus status);
}
