package com.corelate.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SessionFormPairRequestDto {

    @NotBlank
    private String sessionId;

    @NotBlank
    private String formId;

    @NotBlank
    private String mockAppId;

    @NotBlank
    private String workflowId;
}
