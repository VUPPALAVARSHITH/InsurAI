package com.insurai.backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.insurai.backend.model.Agent;
import com.insurai.backend.model.AgentAvailability;

@Repository
public interface AgentAvailabilityRepository extends JpaRepository<AgentAvailability, Long> {
    Optional<AgentAvailability> findTopByAgentOrderByIdDesc(Agent agent);
}