package com.corelate.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
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

    private String certificatePdfPath;

    private String createdByEmail;

    private String applicationType;

    private String formId;

    @Column(columnDefinition = "TEXT")
    private String pageMessage;

    private String workflowId;

    @OneToMany(mappedBy = "mockApp", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MockAppPdfFieldMapping> pdfFieldMappings = new ArrayList<>();
}
