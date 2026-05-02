package com.corelate.app.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "mock_app_certificate_field_mapping")
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPdfField() {
        return pdfField;
    }

    public void setPdfField(String pdfField) {
        this.pdfField = pdfField;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getFallbackValue() {
        return fallbackValue;
    }

    public void setFallbackValue(String fallbackValue) {
        this.fallbackValue = fallbackValue;
    }

    public MockApp getMockApp() {
        return mockApp;
    }

    public void setMockApp(MockApp mockApp) {
        this.mockApp = mockApp;
    }
}
