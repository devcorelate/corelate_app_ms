package com.corelate.app.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SessionDataDto extends BaseDto {

    @NotBlank(message = "sessionId is required")
    private String sessionId;

    @NotBlank(message = "workflowId is required")
    private String workflowId;

    private String startedAt;

    private String lastUpdatedAt;

    private String currentNodeId;

    private JsonNode steps;

    private JsonNode gatewayDecisions;

    private String returnTo;
}
