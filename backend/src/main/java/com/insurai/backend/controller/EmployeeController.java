package com.insurai.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insurai.backend.repository.AgentRepository;
import com.insurai.backend.repository.PolicyRepository;
import com.insurai.backend.service.PolicyService;

@RestController
@RequestMapping("/employee")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class EmployeeController {

    @Autowired
    private PolicyService policyService;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private PolicyRepository policyRepository;

    // -------------------- Get All Policies (Catalog) --------------------
    // This fills the "Select Policy" dropdown for Claims & Queries
    @GetMapping("/policies")
    public ResponseEntity<?> getAllPolicies() {
        // We use the repository directly to ensure we get ALL options (Health, Auto, Life)
        return ResponseEntity.ok(policyRepository.findAll());
    }

    // -------------------- Get All Agents --------------------
    // This fills the "Select Agent" dropdown for "Ask a Query"
    @GetMapping("/agents")
    public ResponseEntity<?> getAllAgents() {
        return ResponseEntity.ok(agentRepository.findAll());
    }
}