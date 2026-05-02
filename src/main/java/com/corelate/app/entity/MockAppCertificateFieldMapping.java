package com.corelate.app.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "mock_app_certificate_field_mapping")
@Data
public class MockAppCertificateFieldMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String appId;

    @Column(nullable = false)
    private String workflowId;

    @Column(nullable = false)
    private String formId;

    @Column(nullable = false)
    private String sourcePath;

    @Column(nullable = false)
    private String targetField;
}
