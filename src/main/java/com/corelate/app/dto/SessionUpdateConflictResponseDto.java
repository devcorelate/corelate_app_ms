package com.corelate.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SessionUpdateConflictResponseDto {
    private String errorCode;
    private String errorMessage;
    private String sessionId;
    private LocalDateTime timestamp;
}
