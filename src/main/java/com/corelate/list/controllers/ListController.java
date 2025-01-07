package com.corelate.list.controllers;

import com.corelate.list.constants.AppConstants;
import com.corelate.list.dto.ErrorResponseDto;
import com.corelate.list.dto.ListDto;
import com.corelate.list.dto.ResponseDto;
import com.corelate.list.service.IListService;
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
        name = "CRUD REST APIs for Orchestrator in Corelate",
        description = "CRUD REST APIs in Corelate to CREATE, UPDATE, FETCH AND DELETE orchestrator details"
)

@RestController
@RequestMapping(path="/list", produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class ListController {

    private static final Logger logger = LoggerFactory.getLogger(ListController.class);

    private final IListService iListService;

    public ListController(IListService iListService) {
        this.iListService = iListService;
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
        iListService.createList(ListDto);
        logger.debug("createList method end");
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
        List<ListDto> ListDtos = iListService.fetchAllList();
        logger.debug("fetchAllLists method end");
        return ResponseEntity.status(HttpStatus.OK).body(ListDtos);
    }
}
