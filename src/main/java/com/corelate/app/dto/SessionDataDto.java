package com.corelate.app.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class SessionDataDto extends BaseDto {

    @NotBlank(message = "sessionId is required")
    private String sessionId;

    @NotBlank(message = "workflowId is required")
    private String workflowId;

    private Instant startedAt;

    private Instant lastUpdatedAt;

    private String currentNodeId;

    @Valid
    private Map<String, SessionStepDto> steps;

    private Map<String, JsonNode> gatewayDecisions;

    private String returnTo;

    @Data
    public static class SessionStepDto {

        private String elementId;

        private String elementType;

        private String status;

        private JsonNode data;

        private Instant completedAt;
    }
}
