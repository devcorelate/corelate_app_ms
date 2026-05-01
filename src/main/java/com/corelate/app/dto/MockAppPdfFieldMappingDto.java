package com.corelate.app.dto;

import lombok.Data;

@Data
public class MockAppPdfFieldMappingDto {

    private String pdfField;

    private String sourcePath;

    private String fallbackValue;
}
