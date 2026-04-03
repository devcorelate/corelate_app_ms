package com.corelate.app.service;

import com.corelate.app.dto.SessionDataDto;

import java.util.List;

public interface ISessionDataService {

    void addSessionData(SessionDataDto sessionDataDto);

    void updateSessionData(String sessionId, SessionDataDto sessionDataDto);

    void deleteSessionData(String sessionId);

    void deleteAllSessionData();

    List<SessionDataDto> fetchAllSessionData();

    SessionDataDto fetchSessionDataById(String sessionId);
}
