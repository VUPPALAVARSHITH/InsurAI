package com.insurai.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.insurai.backend.config.JwtUtil;
import com.insurai.backend.model.Agent;
import com.insurai.backend.model.LoginRequest;
import com.insurai.backend.repository.AgentRepository;

@RestController
@RequestMapping("/agent")
@CrossOrigin(origins = "http://localhost:5173")
public class AgentController {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // -------------------- Agent Login --------------------
    @PostMapping("/login")
    public ResponseEntity<?> agentLogin(@RequestBody LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        // 1. Find Agent in Database
        Agent agent = agentRepository.findByEmail(email).orElse(null);

        if (agent == null) {
            return ResponseEntity.status(404).body("Agent not found with email: " + email);
        }

        // 2. Compare Password (Input vs Database Hash)
        if (passwordEncoder.matches(password, agent.getPassword())) {

            // 3. Generate Token
            String token = jwtUtil.generateToken(email, "AGENT");

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "name", agent.getName(),
                    "role", "AGENT",
                    "token", token,
                    "agentId", agent.getId(),
                    "isAvailable", agent.isAvailable()
            ));
        } else {
            return ResponseEntity.status(403).body("Invalid agent credentials (Password mismatch)");
        }
    }

    // -------------------- Toggle Availability (FIXED) --------------------
    @PutMapping("/toggle-availability/{id}")
    public ResponseEntity<?> toggleAvailability(@PathVariable Long id) {
        Agent agent = agentRepository.findById(id).orElse(null);

        if (agent == null) {
            return ResponseEntity.status(404).body("Agent not found");
        }

        // Flip the current status
        boolean newStatus = !agent.isAvailable();

        // ✅ FIXED: Only use setAvailable.
        // This works for both 'available' and 'isAvailable' fields.
        agent.setAvailable(newStatus);

        agentRepository.save(agent);

        return ResponseEntity.ok(Map.of(
                "message", "Availability updated successfully",
                "isAvailable", newStatus
        ));
    }

    // -------------------- Get Agent Profile/Status --------------------
    @GetMapping("/{id}")
    public ResponseEntity<?> getAgentProfile(@PathVariable Long id) {
        Agent agent = agentRepository.findById(id).orElse(null);
        if (agent == null) return ResponseEntity.status(404).body("Agent not found");

        return ResponseEntity.ok(agent);
    }
}