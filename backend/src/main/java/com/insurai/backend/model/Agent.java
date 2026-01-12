package com.insurai.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "agents")
@Data
public class Agent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String role; // "AGENT"

    // ✅ FIX: Use only ONE field for availability.
    // This maps the Java variable 'available' to the SQL column 'is_available'.
    // Lombok will correctly generate 'setAvailable()' for this.
    @Column(name = "is_available")
    private boolean available = true;
}