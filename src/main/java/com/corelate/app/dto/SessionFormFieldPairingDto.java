package com.corelate.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SessionFormFieldPairingDto {

    private String sessionId;
    private String workflowId;
    private String formId;
    private String sourcePath;
    private String targetField;
    private String value;
    private String pdfTitle;
    private String pdfDescription;
}
