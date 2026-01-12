package com.insurai.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insurai.backend.model.Employee;
import com.insurai.backend.model.EmployeeQuery;
import com.insurai.backend.repository.EmployeeRepository;
import com.insurai.backend.service.EmployeeQueryService;

@RestController
@RequestMapping("/employee/queries")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class EmployeeQueryController {

    private final EmployeeQueryService queryService;
    private final EmployeeRepository employeeRepository;

    public EmployeeQueryController(EmployeeQueryService queryService, EmployeeRepository employeeRepository) {
        this.queryService = queryService;
        this.employeeRepository = employeeRepository;
    }

    // 1. Get MY queries (Auto-detects user from token)
    @GetMapping
    public ResponseEntity<List<EmployeeQuery>> getMyQueries() {
        String email = getCurrentUserEmail();
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return ResponseEntity.ok(queryService.getQueriesForEmployee(employee.getId()));
    }

    // 2. Submit a new query
    @PostMapping
    public ResponseEntity<EmployeeQuery> submitQuery(@RequestBody QueryRequest request) {
        try {
            String email = getCurrentUserEmail();
            Employee employee = employeeRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            // Call the service method you provided
            EmployeeQuery query = queryService.submitQuery(
                    employee.getId(),
                    request.agentId,
                    request.queryText,
                    request.policyName,
                    request.claimType
            );
            return ResponseEntity.ok(query);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Helper to get email from the "ID Badge" (Token)
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    // Helper class to read the JSON sent from React
    static class QueryRequest {
        public Long agentId;
        public String queryText;
        public String policyName;
        public String claimType;
    }
}