package com.insurai.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "claim_status_history")
public class ClaimStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "claim_id")
    private Claim claim;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String changedByRole; // EMPLOYEE / HR / ADMIN

    private String remarks;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    public ClaimStatusHistory() {}

    public ClaimStatusHistory(
            Claim claim,
            String status,
            String changedByRole,
            String remarks
    ) {
        this.claim = claim;
        this.status = status;
        this.changedByRole = changedByRole;
        this.remarks = remarks;
        this.changedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public Claim getClaim() { return claim; }
    public String getStatus() { return status; }
    public String getChangedByRole() { return changedByRole; }
    public String getRemarks() { return remarks; }
    public LocalDateTime getChangedAt() { return changedAt; }
}
