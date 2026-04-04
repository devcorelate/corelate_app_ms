package com.corelate.app.service;

public interface SessionUpdateSynchronizationHook {

    void afterLoadBeforeUpdate(String sessionId);
}
