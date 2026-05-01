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

    private String pdfField;

    private String sourcePath;

    @Column(columnDefinition = "TEXT")
    private String fallbackValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mock_app_id", nullable = false)
    private MockApp mockApp;
}
