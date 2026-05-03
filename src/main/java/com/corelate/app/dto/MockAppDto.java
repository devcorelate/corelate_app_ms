package com.corelate.app.dto;

import lombok.Data;

import java.util.List;

@Data
public class MockAppDto extends BaseDto {

    private String appId;

    private String name;

    private String description;

    private String role;

    private String audience;

    private String applicationType;

    private String certificatePdfPath;

    private String pdfTitle;

    private String pdfDescription;

    private String createdByEmail;

    private String formId;

    private String pageMessage;

    private String workflowId;

    private List<MockAppCertificateFieldMappingDto> certificateFieldMappings;
}
