package com.insurai.backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.insurai.backend.model.Agent;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    // ✅ This finds the agent by email so we can check their password
    Optional<Agent> findByEmail(String email);
}