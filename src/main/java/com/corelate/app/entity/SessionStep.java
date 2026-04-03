package com.corelate.app.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "session_step")
@Data
public class SessionStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String stepKey;

    private String elementId;

    private String elementType;

    private String status;

    private Instant completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_data_id", nullable = false)
    private SessionData sessionData;

    @OneToOne(mappedBy = "sessionStep", cascade = CascadeType.ALL, orphanRemoval = true)
    private SessionElementData sessionElementData;
}
