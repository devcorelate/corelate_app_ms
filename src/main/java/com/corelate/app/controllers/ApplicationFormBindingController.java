package com.corelate.app.controllers;

import com.corelate.app.dto.FormBindingBatchRequestDto;
import com.corelate.app.dto.FormBindingDto;
import com.corelate.app.service.IAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(
        name = "Application Form Bindings API",
        description = "APIs for resolving application to form bindings"
)
@RestController
@RequestMapping(path = "/api/applications", produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class ApplicationFormBindingController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationFormBindingController.class);

    private final IAppService iAppService;

    public ApplicationFormBindingController(IAppService iAppService) {
        this.iAppService = iAppService;
    }

    @Operation(summary = "Fetch application form bindings in batch")
    @PostMapping("/form-bindings/batch")
    public ResponseEntity<List<FormBindingDto>> fetchFormBindingsBatch(
            @Valid @RequestBody FormBindingBatchRequestDto requestDto) {
        logger.debug("fetchFormBindingsBatch method start");
        List<FormBindingDto> response = iAppService.fetchFormBindingsByAppIds(requestDto.getAppIds());
        logger.debug("fetchFormBindingsBatch method end");
        return ResponseEntity.ok(response);
    }
}
