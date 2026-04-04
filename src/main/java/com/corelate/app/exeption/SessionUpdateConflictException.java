package com.corelate.app.exeption;

import lombok.Getter;

@Getter
public class SessionUpdateConflictException extends RuntimeException {

    private final String sessionId;

    public SessionUpdateConflictException(String sessionId, Throwable cause) {
        super("Session was modified by another transaction.", cause);
        this.sessionId = sessionId;
    }
}
