package com.insurai.backend.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.insurai.backend.model.Employee;
import com.insurai.backend.model.Hr;
import com.insurai.backend.model.RegisterRequest;
import com.insurai.backend.repository.ClaimRepository;
import com.insurai.backend.repository.EmployeeRepository;
import com.insurai.backend.repository.HrRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HrService {

    private final HrRepository hrRepository;
    private final EmployeeRepository employeeRepository;
    private final ClaimRepository claimRepository;
    private final PasswordEncoder passwordEncoder;

    // ---------------- Register HR ----------------
    public Hr registerHR(RegisterRequest request) {
        Hr hr = new Hr();
        hr.setName(request.getName());
        hr.setEmail(request.getEmail());
        hr.setPhoneNumber(request.getPhoneNumber());
        hr.setHrId(request.getHrId());
        hr.setPassword(passwordEncoder.encode(request.getPassword()));
        return hrRepository.save(hr);
    }

    public Optional<Hr> findByEmail(String email) {
        return hrRepository.findByEmail(email);
    }

    public boolean validateCredentials(String email, String rawPassword) {
        return hrRepository.findByEmail(email)
                .map(hr -> passwordEncoder.matches(rawPassword, hr.getPassword()))
                .orElse(false);
    }

    public List<Hr> getAllActiveHrs() {
        return hrRepository.findAll();
    }

    public String getEmployeeNameById(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .map(Employee::getName)
                .orElse("Unknown Employee");
    }

    // ✅ NEW: HR Workload Counter
    public Map<Long, Long> getHrPendingClaimCounts() {

        Map<Long, Long> workloadMap = new HashMap<>();

        List<Object[]> results = claimRepository.countPendingClaimsPerHr();

        for (Object[] row : results) {
            Long hrId = (Long) row[0];
            Long count = (Long) row[1];
            workloadMap.put(hrId, count);
        }

        return workloadMap;
    }
}
