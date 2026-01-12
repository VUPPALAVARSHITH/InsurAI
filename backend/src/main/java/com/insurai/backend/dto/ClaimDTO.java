package com.insurai.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.insurai.backend.model.Claim;

public class ClaimDTO {

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
