package com.insurai.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.insurai.backend.model.Claim;
import com.insurai.backend.model.Employee;
import com.insurai.backend.model.Hr;
import com.insurai.backend.model.Policy;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByEmployee(Employee employee);

    List<Claim> findByEmployee_EmployeeId(String employeeId);

    List<Claim> findByPolicy(Policy policy);

    List<Claim> findByStatus(String status);

    List<Claim> findByEmployeeAndStatus(Employee employee, String status);

    List<Claim> findByEmployee_EmployeeIdAndStatus(String employeeId, String status);

    List<Claim> findByAssignedHrId(Long hrId);

    int countByAssignedHrAndStatus(Hr hr, String status);

    @Query("SELECT c FROM Claim c JOIN FETCH c.employee WHERE c.id = :claimId")
    Optional<Claim> findByIdWithEmployee(@Param("claimId") Long claimId);

    @Query("SELECT c FROM Claim c LEFT JOIN FETCH c.assignedHr")
    List<Claim> findAllWithHrDetails();

    List<Claim> findByAssignedHrIdAndFraudFlag(Long hrId, boolean fraudFlag);

    // ✅ NEW: Count pending claims per HR (WORKLOAD)
    @Query("""
        SELECT c.assignedHr.id, COUNT(c)
        FROM Claim c
        WHERE c.status = 'PENDING'
        GROUP BY c.assignedHr.id
    """)
    List<Object[]> countPendingClaimsPerHr();
}
