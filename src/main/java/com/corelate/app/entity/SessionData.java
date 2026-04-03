package com.corelate.app.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

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

    private String startedAt;

    private String lastUpdatedAt;

    private String currentNodeId;

    @OneToMany(mappedBy = "sessionData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SessionStep> steps = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String gatewayDecisions;

    private String returnTo;
}
