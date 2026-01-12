package com.insurai.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder; // ✅ Added
import org.springframework.web.bind.annotation.*;

import com.insurai.backend.config.JwtUtil;
import com.insurai.backend.model.*;
import com.insurai.backend.repository.AdminRepository; // ✅ Added
import com.insurai.backend.service.*;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminRepository adminRepository; // ✅ Direct access to DB for login

    @Autowired
    private PasswordEncoder passwordEncoder; // ✅ To check hashed passwords

    @Autowired
    private PolicyService policyService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ClaimService claimService;

    @Autowired
    private AuditLogService auditLogService;

    // -------------------- Admin Login (FIXED) --------------------
    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        // 1. Find Admin by Email
        Admin admin = adminRepository.findByEmail(email).orElse(null);

        // 2. Check if Admin exists AND Password matches Hash
        if (admin != null && passwordEncoder.matches(password, admin.getPassword())) {

            // Generate Token
            String token = jwtUtil.generateToken(email, "ADMIN");

            return ResponseEntity.ok(new LoginResponse(
                    "Login successful",
                    "Super Admin", // You can use admin.getName() if your Admin model has a name
                    "ADMIN",
                    token
            ));
        } else {
            return ResponseEntity.status(403).body("Invalid admin credentials");
        }
    }

    // -------------------- Register Agent --------------------
    @PostMapping("/agent/register")
    public ResponseEntity<?> registerAgent(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody RegisterRequest registerRequest) {

        if (!isAdminJwt(authHeader)) {
            return ResponseEntity.status(403).body("Access denied. Please login as Admin.");
        }

        adminService.registerAgent(registerRequest);
        return ResponseEntity.ok("Agent registered successfully");
    }

    // -------------------- Register HR --------------------
    @PostMapping("/hr/register")
    public ResponseEntity<?> registerHR(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody RegisterRequest registerRequest) {

        if (!isAdminJwt(authHeader)) {
            return ResponseEntity.status(403).body("Access denied. Please login as Admin.");
        }

        adminService.registerHR(registerRequest);
        return ResponseEntity.ok("HR registered successfully");
    }

    // -------------------- Get All Claims --------------------
    @GetMapping("/claims")
    public ResponseEntity<?> getAllClaims(@RequestHeader(value = "Authorization") String authHeader) {
        try {
            if (!isAdminJwt(authHeader)) {
                return ResponseEntity.status(403).body("Access denied. Please login as Admin.");
            }

            List<Claim> claims = claimService.getAllClaims();
            List<ClaimDTO> dtos = claims.stream()
                    .map(ClaimDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching claims: " + e.getMessage());
        }
    }

    // -------------------- JWT Validation Helper --------------------
    private boolean isAdminJwt(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String role = jwtUtil.extractRole(token);
            return "ADMIN".equalsIgnoreCase(role);
        }
        return false;
    }

    // ================= Get All Fraud-Flagged Claims (Admin) =================
    @GetMapping("/claims/fraud")
    public ResponseEntity<?> getFraudClaimsAdmin(@RequestHeader(value = "Authorization") String authHeader) {
        try {
            if (!isAdminJwt(authHeader)) {
                return ResponseEntity.status(403).body("Access denied. Please login as Admin.");
            }

            List<Claim> claims = claimService.getAllClaims()
                    .stream()
                    .filter(Claim::isFraud)
                    .collect(Collectors.toList());

            List<ClaimDTO> dtos = claims.stream()
                    .map(ClaimDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching fraud claims: " + e.getMessage());
        }
    }

    // ================= Get All Audit Logs =================
    @GetMapping("/audit/logs")
    public ResponseEntity<?> getAllAuditLogs(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(403).body("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7).trim();
            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equalsIgnoreCase(role)) {
                return ResponseEntity.status(403).body("Unauthorized: Not an admin");
            }

            List<AuditLog> logs = auditLogService.getAllLogs();
            return ResponseEntity.ok(logs);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching audit logs: " + e.getMessage());
        }
    }

    // -------------------- Inner Classes --------------------
    public static class LoginResponse {
        private String message;
        private String name;
        private String role;
        private String token;

        public LoginResponse(String message, String name, String role, String token) {
            this.message = message;
            this.name = name;
            this.role = role;
            this.token = token;
        }

        public String getMessage() { return message; }
        public String getName() { return name; }
        public String getRole() { return role; }
        public String getToken() { return token; }
    }

    public static class ClaimDTO {
        // (Keep your existing ClaimDTO code here - same as you provided)
        // I am omitting it for brevity, but MAKE SURE TO KEEP IT!
        private Long id;
        private String title;
        private String description;
        private Double amount;
        private String status;
        private String remarks;
        private java.time.LocalDateTime claimDate;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
        private Long employeeId;
        private String employeeName;
        private Long policyId;
        private String policyName;
        private java.util.List<String> documents;
        private Long assignedHrId;
        private String assignedHrName;
        private boolean fraudFlag;
        private String fraudReason;

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
            if (claim.getEmployee() != null) {
                this.employeeId = claim.getEmployee().getId();
                this.employeeName = claim.getEmployee().getName();
            }
            if (claim.getAssignedHr() != null) {
                this.assignedHrId = claim.getAssignedHr().getId();
                this.assignedHrName = claim.getAssignedHr().getName();
            }
            if (claim.getPolicy() != null) {
                this.policyId = claim.getPolicy().getId();
                this.policyName = claim.getPolicy().getPolicyName();
            } else {
                this.policyName = "N/A";
            }
            this.documents = claim.getDocuments();
            this.fraudFlag = claim.isFraud();
            this.fraudReason = claim.getFraudReason();
        }

        // Getters...
        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public Double getAmount() { return amount; }
        public String getStatus() { return status; }
        public String getRemarks() { return remarks; }
        public java.time.LocalDateTime getClaimDate() { return claimDate; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
        public Long getEmployeeId() { return employeeId; }
        public String getEmployeeName() { return employeeName; }
        public Long getPolicyId() { return policyId; }
        public String getPolicyName() { return policyName; }
        public java.util.List<String> getDocuments() { return documents; }
        public Long getAssignedHrId() { return assignedHrId; }
        public String getAssignedHrName() { return assignedHrName; }
        public boolean isFraudFlag() { return fraudFlag; }
        public String getFraudReason() { return fraudReason; }
    }
}