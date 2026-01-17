package com.corelate.app.controllers;

import com.corelate.app.constants.AppConstants;
import com.corelate.app.dto.*;
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

/**
 * @author Seth Hernandez
 */

@Tag(
        name = "CRUD REST APIs for Application Builder in Corelate",
        description = "CRUD REST APIs in Corelate to CREATE, UPDATE, FETCH AND DELETE Applicaiton Builder details"
)
@RestController
@RequestMapping(path="/data", produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class AppController {

    private static final Logger logger = LoggerFactory.getLogger(AppController.class);

    private final IAppService iAppService;

    public AppController(IAppService iAppService) {
        this.iAppService = iAppService;
    }

    @Operation(
            summary = "Create List REST API",
            description = "REST API to create new List database Corelate"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "HTTP Status CREATED"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP Status Internal Server Error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    }
    )
    @PostMapping("/create")
    public ResponseEntity<ResponseDto> createList(@Valid @RequestBody ListDto ListDto) {
        logger.debug("createList method start");
        iAppService.createList(ListDto);
        logger.debug("createList method end");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(AppConstants.STATUS_201, AppConstants.MESSAGE_201));
    }

    @Operation(
            summary = "Create List REST API",
            description = "REST API to create new List database Corelate"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "HTTP Status CREATED"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP Status Internal Server Error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    }
    )
    @PostMapping("/add/form-data")
    public ResponseEntity<ResponseDto> createFormDataList(@Valid @RequestBody FormDataDto formDataDto) {
        logger.debug("createList method start");
        iAppService.createFormDataList(formDataDto);
        logger.debug("createList method end");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(AppConstants.STATUS_201, AppConstants.MESSAGE_201));
    }


    @Operation(
            summary = "Get All Workflow Data REST API",
            description = "REST API to Get All Workflow"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "HTTP Status CREATED"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP Status Internal Server Error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    }
    )
    @GetMapping("/fetch/all/data-by-workflow")
    public ResponseEntity<List<FormDataDto>> getAllDataByWorkflowId(@RequestParam String templateId) {
        logger.debug("getAllDataByWorkflowId method start");
        List<FormDataDto> formDataDtos = iAppService.getAllDataByWorkflowId(templateId);
        logger.debug("getAllDataByWorkflowId method end");
        return ResponseEntity.status(HttpStatus.OK).body(formDataDtos);
    }

    @Operation(
            summary = "Create List REST API",
            description = "REST API to create new publishLog"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "HTTP Status CREATED"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP Status Internal Server Error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    }
    )
    @PutMapping("/update/application/publish")
    public ResponseEntity<ResponseDto> addPublishLog(@Valid @RequestBody PublishDto publishDto) {
        logger.debug("publishLog method start");
        iAppService.publishApplication(publishDto);
        logger.debug("publishLog method end");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(AppConstants.STATUS_201, AppConstants.MESSAGE_201));
    }

    @Operation(
            summary = "Create List REST API",
            description = "REST API to create new publishLog"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "HTTP Status CREATED"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP Status Internal Server Error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    }
    )
    @GetMapping("/fetch/all/published")
    public ResponseEntity<List<PublishDtoEmail> > getAllPublishedApplication() {
        logger.debug("publishLog method start");
        List<PublishDtoEmail> publishDtos = iAppService.getLatestUniquePublishedLogs();
        logger.debug("publishLog method end");
        return ResponseEntity.status(HttpStatus.OK).body(publishDtos);
    }

    @Operation(
            summary = "Create Application REST API",
            description = "REST API to create new List database Corelate"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "HTTP Status CREATED"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP Status Internal Server Error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    }
    )
    @PostMapping("/update")
    public ResponseEntity<ResponseDto> updateFormData(@Valid @RequestBody FormUpdateRequestDto formUpdateRequestDto) {
        logger.debug("updateApplicationFormData method start");
        iAppService.updateFormData(formUpdateRequestDto);
        logger.debug("updateApplicationFormData method end");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(AppConstants.STATUS_201, AppConstants.MESSAGE_201));
    }

    @Operation(
            summary = "Fetch List Templates Details REST API",
            description = "REST API to fetch all List Templates"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP Status OK"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP Status Internal Server Error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    }
    )
    @GetMapping("/fetch/all")
    public ResponseEntity<List<ListDto>> fetchAllLists() {
        logger.debug("fetchAllLists method start");
        List<ListDto> ListDtos = iAppService.fetchAllList();
        logger.debug("fetchAllLists method end");
        return ResponseEntity.status(HttpStatus.OK).body(ListDtos);
    }
}
