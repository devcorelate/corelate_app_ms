package com.corelate.app.controllers;

import com.corelate.app.constants.AppConstants;
import com.corelate.app.dto.ResponseDto;
import com.corelate.app.service.ISessionElementDataService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(
        name = "Session Element Data APIs in Corelate",
        description = "REST APIs in Corelate to fetch and update SessionElementData payload"
)
@RestController
@RequestMapping(path = "/session-element-data", produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class SessionElementDataController {

    private static final Logger logger = LoggerFactory.getLogger(SessionElementDataController.class);

    private final ISessionElementDataService sessionElementDataService;

    public SessionElementDataController(ISessionElementDataService sessionElementDataService) {
        this.sessionElementDataService = sessionElementDataService;
    }

    @Operation(summary = "Fetch all SessionElementData payloads", description = "REST API to fetch all data payloads")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HTTP Status OK"),
            @ApiResponse(responseCode = "500", description = "HTTP Status Internal Server Error", content = @Content(schema = @Schema(implementation = com.corelate.app.dto.ErrorResponseDto.class)))
    })
    @GetMapping("/fetch/data/all")
    public ResponseEntity<List<JsonNode>> fetchAllData() {
        logger.debug("fetchAllData method start");
        List<JsonNode> data = sessionElementDataService.fetchAllData();
        logger.debug("fetchAllData method end");
        return ResponseEntity.status(HttpStatus.OK).body(data);
    }

    @Operation(summary = "Fetch SessionElementData payloads by workflowId", description = "REST API to fetch all data payloads by workflowId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HTTP Status OK"),
            @ApiResponse(responseCode = "500", description = "HTTP Status Internal Server Error", content = @Content(schema = @Schema(implementation = com.corelate.app.dto.ErrorResponseDto.class)))
    })
    @GetMapping("/fetch/data/{workflowId}")
    public ResponseEntity<List<JsonNode>> fetchAllDataByWorkflowId(@PathVariable @NotBlank String workflowId) {
        logger.debug("fetchAllDataByWorkflowId method start");
        List<JsonNode> data = sessionElementDataService.fetchAllDataByWorkflowId(workflowId);
        logger.debug("fetchAllDataByWorkflowId method end");
        return ResponseEntity.status(HttpStatus.OK).body(data);
    }

    @Operation(summary = "Update SessionElementData payload values by workflowId", description = "REST API to update payload values by id/value map for all records in a workflow")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HTTP Status OK"),
            @ApiResponse(responseCode = "500", description = "HTTP Status Internal Server Error", content = @Content(schema = @Schema(implementation = com.corelate.app.dto.ErrorResponseDto.class)))
    })
    @PutMapping("/update/data/{workflowId}")
    public ResponseEntity<ResponseDto> updateDataByWorkflowId(@PathVariable @NotBlank String workflowId,
                                                              @RequestBody Map<String, JsonNode> updates) {
        logger.debug("updateDataByWorkflowId method start");
        sessionElementDataService.updateDataByWorkflowId(workflowId, updates);
        logger.debug("updateDataByWorkflowId method end");
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseDto(AppConstants.STATUS_200, AppConstants.MESSAGE_200));
    }
}
