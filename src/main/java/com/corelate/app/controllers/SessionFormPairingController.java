package com.corelate.app.controllers;

import com.corelate.app.dto.SessionFormFieldPairingDto;
import com.corelate.app.dto.SessionFormPairRequestDto;
import com.corelate.app.dto.SessionFormPairResultDto;
import com.corelate.app.service.ISessionFormPairingService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/session-form-mappings", produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class SessionFormPairingController {

    private final ISessionFormPairingService pairingService;

    public SessionFormPairingController(ISessionFormPairingService pairingService) {
        this.pairingService = pairingService;
    }

    @PostMapping("/pair")
    public ResponseEntity<SessionFormPairResultDto> pairSessionFormData(
            @Valid @RequestBody SessionFormPairRequestDto requestDto) {
        return ResponseEntity.ok(pairingService.pairSessionFormData(requestDto));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<List<SessionFormFieldPairingDto>> fetchSessionFormMappings(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(pairingService.fetchBySessionId(sessionId));
    }
}
