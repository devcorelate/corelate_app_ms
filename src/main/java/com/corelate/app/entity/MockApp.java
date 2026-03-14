package com.corelate.app.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "mock_app")
@Data
public class MockApp extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String appId;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String role;

    private String audience;

    private String applicationType;

    private String formId;

    @Column(columnDefinition = "TEXT")
    private String pageMessage;

    private String workflowId;
}
