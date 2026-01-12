package com.insurai.backend.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.insurai.backend.model.*;
import com.insurai.backend.repository.*;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final HrService hrService;
    private final FraudService fraudService;
    private final ClaimStatusHistoryRepository historyRepository;

    public ClaimService(
            ClaimRepository claimRepository,
            HrService hrService,
            FraudService fraudService,
            ClaimStatusHistoryRepository historyRepository
    ) {
        this.claimRepository = claimRepository;
        this.hrService = hrService;
        this.fraudService = fraudService;
        this.historyRepository = historyRepository;
    }

    // ================= SUBMIT =================
    public Claim submitClaim(Claim claim) {

        validateClaimAmount(claim);

        claim.setStatus("PENDING");
        claim.setCreatedAt(LocalDateTime.now());
        claim.setUpdatedAt(LocalDateTime.now());

        runFraudDetection(claim);
        assignHrAutomatically(claim);

        Claim saved = claimRepository.save(claim);

        // 🔥 Timeline entry
        historyRepository.save(
                new ClaimStatusHistory(
                        saved,
                        "PENDING",
                        "EMPLOYEE",
                        "Claim submitted"
                )
        );

        return saved;
    }

    // ================= APPROVE =================
    public Claim approveClaim(Long claimId, String remarks) {

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        claim.setStatus("APPROVED");
        claim.setRemarks(remarks);
        claim.setUpdatedAt(LocalDateTime.now());

        Claim updated = claimRepository.save(claim);

        historyRepository.save(
                new ClaimStatusHistory(
                        updated,
                        "APPROVED",
                        "HR",
                        remarks
                )
        );

        return updated;
    }

    // ================= REJECT =================
    public Claim rejectClaim(Long claimId, String remarks) {

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        claim.setStatus("REJECTED");
        claim.setRemarks(remarks);
        claim.setUpdatedAt(LocalDateTime.now());

        Claim updated = claimRepository.save(claim);

        historyRepository.save(
                new ClaimStatusHistory(
                        updated,
                        "REJECTED",
                        "HR",
                        remarks
                )
        );

        return updated;
    }

    // ================= READ =================
    public List<Claim> getClaimsByEmployee(Employee employee) {
        return claimRepository.findByEmployee(employee);
    }

    public List<Claim> getClaimsByAssignedHr(Long hrId) {
        return claimRepository.findByAssignedHrId(hrId);
    }

    public List<ClaimStatusHistory> getTimeline(Long claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        return historyRepository.findByClaimOrderByChangedAtAsc(claim);
    }

    // ================= HELPERS =================
    private void validateClaimAmount(Claim claim) {
        if (claim.getAmount() > claim.getPolicy().getCoverageAmount()) {
            throw new IllegalArgumentException("Amount exceeds coverage");
        }
    }

    private void runFraudDetection(Claim claim) {
        try {
            fraudService.runFraudDetection(
                    claim,
                    claimRepository.findByEmployee(claim.getEmployee())
            );
        } catch (Exception e) {
            claim.setFraudFlag(false);
        }
    }

    private void assignHrAutomatically(Claim claim) {
        hrService.getAllActiveHrs().stream()
                .min(Comparator.comparingInt(
                        hr -> claimRepository.countByAssignedHrAndStatus(hr, "PENDING")
                ))
                .ifPresent(claim::setAssignedHr);
    }

    // ================= ADMIN: GET ALL CLAIMS =================
    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

}
