package com.insurai.backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.insurai.backend.model.Claim;
import com.insurai.backend.model.Employee;
import com.insurai.backend.model.Policy;
import com.insurai.backend.repository.EmployeeRepository;
import com.insurai.backend.repository.PolicyRepository;
import com.insurai.backend.service.*;

@RestController
@RequestMapping("/employee/claims")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ClaimController {

    private final ClaimService claimService;
    private final EmployeeRepository employeeRepository;
    private final PolicyRepository policyRepository;
    private final InAppNotificationService notificationService;
    private final AuditLogService auditLogService;
    private final FileStorageService fileStorageService;

    public ClaimController(
            ClaimService claimService,
            EmployeeRepository employeeRepository,
            PolicyRepository policyRepository,
            InAppNotificationService notificationService,
            AuditLogService auditLogService,
            FileStorageService fileStorageService) {

        this.claimService = claimService;
        this.employeeRepository = employeeRepository;
        this.policyRepository = policyRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
        this.fileStorageService = fileStorageService;
    }

    // ---------------- SUBMIT CLAIM ----------------
    @PostMapping
    public ResponseEntity<?> submitClaim(
            @RequestParam Long policyId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam Double amount,
            @RequestParam String date,
            @RequestParam(required = false) List<MultipartFile> documents) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found"));

        List<String> uploadedDocs =
                fileStorageService.storeFiles(documents);

        Claim claim = new Claim(
                title,
                description,
                amount,
                LocalDateTime.parse(date + "T00:00:00"),
                employee,
                policy,
                null,
                uploadedDocs
        );

        Claim saved = claimService.submitClaim(claim);

        notificationService.createNotification(
                "Claim Submitted",
                "Your claim #" + saved.getId() + " has been submitted",
                employee.getId(),
                "EMPLOYEE",
                "CLAIM"
        );

        auditLogService.logAction(
                employee.getId().toString(),
                employee.getName(),
                "EMPLOYEE",
                "SUBMIT_CLAIM",
                "Submitted claim ID " + saved.getId()
        );

        return ResponseEntity.ok(new ClaimDTO(saved));
    }

    // ---------------- GET EMPLOYEE CLAIMS ----------------
    @GetMapping
    public ResponseEntity<?> getEmployeeClaims() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return ResponseEntity.ok(
                claimService.getClaimsByEmployee(employee)
                        .stream()
                        .map(ClaimDTO::new)
                        .collect(Collectors.toList())
        );
    }

    // ---------------- DTO ----------------
    public static class ClaimDTO {

        public Long id;
        public String title;
        public String description;
        public Double amount;
        public String status;
        public String remarks;
        public LocalDateTime claimDate;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
        public Long employeeId;
        public Long policyId;
        public List<String> documents;
        public Long assignedHrId;

        public ClaimDTO(Claim claim) {
            this.id = claim.getId();
            this.title = claim.getTitle();
            this.description = claim.getDescription();
            this.amount = claim.getAmount();
            this.status = claim.getStatus();
            this.remarks = claim.getRemarks();
            this.claimDate = claim.getClaimDate();
            this.createdAt = claim.getCreatedAt();
            this.updatedAt = claim.getUpdatedAt();
            this.employeeId = claim.getEmployee() != null ? claim.getEmployee().getId() : null;
            this.policyId = claim.getPolicy() != null ? claim.getPolicy().getId() : null;
            this.documents = claim.getDocuments();
            this.assignedHrId =
                    claim.getAssignedHr() != null ? claim.getAssignedHr().getId() : null;
        }
    }

    // ---------------- CLAIM TIMELINE ----------------
    @GetMapping("/{claimId}/timeline")
    public ResponseEntity<?> getClaimTimeline(@PathVariable Long claimId) {
        return ResponseEntity.ok(
                claimService.getTimeline(claimId)
        );
    }

}
