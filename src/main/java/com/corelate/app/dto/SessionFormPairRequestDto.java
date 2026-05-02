package com.corelate.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SessionFormPairRequestDto {

    @NotBlank
    private String sessionId;

    private String formId;

    private String mockAppId;

    @NotBlank
    private String workflowId;
}
