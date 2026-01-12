package com.insurai.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insurai.backend.model.Claim;
import com.insurai.backend.model.ClaimStatusHistory;

@Repository
public interface ClaimStatusHistoryRepository
        extends JpaRepository<ClaimStatusHistory, Long> {

    List<ClaimStatusHistory> findByClaimOrderByChangedAtAsc(Claim claim);
}
