package com.corelate.app.controllers;

import com.corelate.app.constants.AppConstants;
import com.corelate.app.dto.ErrorResponseDto;
import com.corelate.app.dto.MockAppDto;
import com.corelate.app.dto.ResponseDto;
import com.corelate.app.service.IAppService;
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

import java.util.List;

@Tag(
        name = "CRUD REST APIs for Mock Apps in Corelate",
        description = "REST APIs in Corelate to CREATE, UPDATE, FETCH, DELETE and SYNC MockApp details"
)
@RestController
@RequestMapping(path = "/mockapps", produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class MockAppController {

    private static final Logger logger = LoggerFactory.getLogger(MockAppController.class);

    private final IAppService iAppService;

    public MockAppController(IAppService iAppService) {
        this.iAppService = iAppService;
    }

    @Operation(summary = "Create Mock App REST API", description = "REST API to create mock app")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "HTTP Status CREATED"),
            @ApiResponse(responseCode = "500", description = "HTTP Status Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/create")
    public ResponseEntity<ResponseDto> createMockApp(@Valid @RequestBody MockAppDto mockAppDto) {
        logger.debug("createMockApp method start");
        iAppService.createMockApp(mockAppDto);
        logger.debug("createMockApp method end");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDto(AppConstants.STATUS_201, AppConstants.MESSAGE_201));
    }

    @Operation(summary = "Update Mock App REST API", description = "REST API to update mock app by appId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HTTP Status OK"),
            @ApiResponse(responseCode = "500", description = "HTTP Status Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping("/update/{appId}")
    public ResponseEntity<ResponseDto> updateMockApp(@PathVariable String appId,
                                                     @Valid @RequestBody MockAppDto mockAppDto) {
        logger.debug("updateMockApp method start");
        iAppService.updateMockApp(appId, mockAppDto);
        logger.debug("updateMockApp method end");
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseDto(AppConstants.STATUS_200, AppConstants.MESSAGE_200));
    }

    @Operation(summary = "Delete Mock App REST API", description = "REST API to delete mock app by appId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HTTP Status OK"),
            @ApiResponse(responseCode = "500", description = "HTTP Status Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/delete/{appId}")
    public ResponseEntity<ResponseDto> deleteMockApp(@PathVariable String appId) {
        logger.debug("deleteMockApp method start");
        iAppService.deleteMockApp(appId);
        logger.debug("deleteMockApp method end");
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseDto(AppConstants.STATUS_200, AppConstants.MESSAGE_200));
    }

    @Operation(summary = "Fetch Mock App REST API", description = "REST API to fetch mock app by appId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HTTP Status OK"),
            @ApiResponse(responseCode = "500", description = "HTTP Status Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/fetch/{appId}")
    public ResponseEntity<MockAppDto> fetchMockApp(@PathVariable String appId) {
        logger.debug("fetchMockApp method start");
        MockAppDto mockAppDto = iAppService.fetchMockApp(appId);
        logger.debug("fetchMockApp method end");
        return ResponseEntity.status(HttpStatus.OK).body(mockAppDto);
    }

    @Operation(summary = "Fetch All Mock Apps REST API", description = "REST API to fetch all mock apps")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HTTP Status OK"),
            @ApiResponse(responseCode = "500", description = "HTTP Status Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/fetchall")
    public ResponseEntity<List<MockAppDto>> fetchAllMockApps() {
        logger.debug("fetchAllMockApps method start");
        List<MockAppDto> mockAppDtos = iAppService.fetchAllMockApps();
        logger.debug("fetchAllMockApps method end");
        return ResponseEntity.status(HttpStatus.OK).body(mockAppDtos);
    }

    @Operation(summary = "Sync Mock Apps REST API", description = "REST API to sync MockApp dataset into database")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HTTP Status OK"),
            @ApiResponse(responseCode = "500", description = "HTTP Status Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/sync")
    public ResponseEntity<ResponseDto> syncMockApps(@RequestBody List<MockAppDto> mockAppDtos) {
        logger.debug("syncMockApps method start");
        iAppService.syncMockApps(mockAppDtos);
        logger.debug("syncMockApps method end");
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseDto(AppConstants.STATUS_200, AppConstants.MESSAGE_200));
    }
}
