package com.corelate.app.service;

import com.corelate.app.dto.SessionFormFieldPairingDto;
import com.corelate.app.dto.SessionFormPairRequestDto;
import com.corelate.app.dto.SessionFormPairResultDto;

import java.util.List;

public interface ISessionFormPairingService {

    SessionFormPairResultDto pairSessionFormData(SessionFormPairRequestDto requestDto);

    List<SessionFormFieldPairingDto> fetchBySessionId(String sessionId);
}
