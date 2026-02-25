package com.corelate.app.service;

import com.corelate.app.dto.SessionDataDto;

public interface ISessionDataService {

    void addSessionData(SessionDataDto sessionDataDto);

    void updateSessionData(String sessionId, SessionDataDto sessionDataDto);

    void deleteSessionData(String sessionId);
}
