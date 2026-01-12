package com.insurai.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.insurai.backend.model.EmployeeQuery;

@Repository
public interface EmployeeQueryRepository extends JpaRepository<EmployeeQuery, Long> {
    List<EmployeeQuery> findByAgentId(Long agentId);
    List<EmployeeQuery> findByEmployeeId(Long employeeId);
    List<EmployeeQuery> findByAgentIdAndStatus(Long agentId, String status);
    List<EmployeeQuery> findByStatus(String status);
}