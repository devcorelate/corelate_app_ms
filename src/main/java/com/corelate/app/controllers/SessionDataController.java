package com.corelate.app.controllers;

import com.corelate.app.constants.AppConstants;
import com.corelate.app.dto.ResponseDto;
import com.corelate.app.dto.SessionDataDto;
import com.corelate.app.service.ISessionDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "CRUD REST APIs for Session Data in Corelate",
        description = "REST APIs in Corelate to ADD, UPDATE and DELETE SessionData"
)
@RestController
@RequestMapping(path = "/session-data", produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class SessionDataController {

    private static final Logger logger = LoggerFactory.getLogger(SessionDataController.class);

    private final ISessionDataService sessionDataService;

    public SessionDataController(ISessionDataService sessionDataService) {
        this.sessionDataService = sessionDataService;
    }

    @Operation(summary = "Add Session Data REST API", description = "REST API to add SessionData")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "HTTP Status CREATED"),
            @ApiResponse(responseCode = "500", description = "HTTP Status Internal Server Error", content = @Content(schema = @Schema(implementation = com.corelate.app.dto.ErrorResponseDto.class)))
    })
    @PostMapping("/add")
    public ResponseEntity<ResponseDto> addSessionData(@Valid @RequestBody SessionDataDto sessionDataDto) {
        logger.debug("addSessionData method start");
        sessionDataService.addSessionData(sessionDataDto);
        logger.debug("addSessionData method end");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDto(AppConstants.STATUS_201, AppConstants.MESSAGE_201));
    }

    @Operation(summary = "Update Session Data REST API", description = "REST API to update SessionData by sessionId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HTTP Status OK"),
            @ApiResponse(responseCode = "500", description = "HTTP Status Internal Server Error", content = @Content(schema = @Schema(implementation = com.corelate.app.dto.ErrorResponseDto.class)))
    })
    @PutMapping("/update/{sessionId}")
    public ResponseEntity<ResponseDto> updateSessionData(@PathVariable String sessionId,
                                                         @Valid @RequestBody SessionDataDto sessionDataDto) {
        logger.debug("updateSessionData method start");
        sessionDataService.updateSessionData(sessionId, sessionDataDto);
        logger.debug("updateSessionData method end");
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseDto(AppConstants.STATUS_200, AppConstants.MESSAGE_200));
    }

    @Operation(summary = "Delete Session Data REST API", description = "REST API to delete SessionData by sessionId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HTTP Status OK"),
            @ApiResponse(responseCode = "500", description = "HTTP Status Internal Server Error", content = @Content(schema = @Schema(implementation = com.corelate.app.dto.ErrorResponseDto.class)))
    })
    @DeleteMapping("/delete/{sessionId}")
    public ResponseEntity<ResponseDto> deleteSessionData(@PathVariable String sessionId) {
        logger.debug("deleteSessionData method start");
        sessionDataService.deleteSessionData(sessionId);
        logger.debug("deleteSessionData method end");
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseDto(AppConstants.STATUS_200, AppConstants.MESSAGE_200));
    }
}
