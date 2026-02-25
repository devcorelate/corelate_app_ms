package com.corelate.app.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "session_data")
@Data
public class SessionData extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sessionId;

    private String workflowId;

    private Instant startedAt;

    private Instant lastUpdatedAt;

    private String currentNodeId;

    @Column(columnDefinition = "TEXT")
    private String steps;

    @Column(columnDefinition = "TEXT")
    private String gatewayDecisions;

    private String returnTo;
}
