package com.corelate.app.dto;

import lombok.Data;

@Data
public class MockAppCertificateFieldMappingDto {

    private String pdfField;

    private String sourcePath;

    private String fallbackValue;

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
}
