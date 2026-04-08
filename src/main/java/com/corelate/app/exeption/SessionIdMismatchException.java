package com.corelate.app.exeption;

public class SessionIdMismatchException extends RuntimeException {

    public SessionIdMismatchException(String pathSessionId, String bodySessionId) {
        super("sessionId in path ('" + pathSessionId + "') does not match body sessionId ('" + bodySessionId + "').");
    }
}
