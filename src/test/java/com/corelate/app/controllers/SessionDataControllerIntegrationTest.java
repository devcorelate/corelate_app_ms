package com.corelate.app.controllers;

import com.corelate.app.dto.SessionDataDto;
import com.corelate.app.exeption.GlobalExceptionHandler;
import com.corelate.app.exeption.ResourceNotFoundException;
import com.corelate.app.exeption.SessionUpdateConflictException;
import com.corelate.app.service.ISessionDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SessionDataController.class)
@Import(GlobalExceptionHandler.class)
class SessionDataControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ISessionDataService sessionDataService;

    @Test
    void updateSessionData_shouldReturn200_whenRequestIsValid() throws Exception {
        doNothing().when(sessionDataService).updateSessionData(eq("session-1"), any(SessionDataDto.class));

        mockMvc.perform(put("/session-data/update/session-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("session-1", "wf-1"))))
                .andExpect(status().isOk());
    }

    @Test
    void updateSessionData_shouldReturn404_whenSessionNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("SessionData", "sessionId", "missing"))
                .when(sessionDataService).updateSessionData(eq("missing"), any(SessionDataDto.class));

        mockMvc.perform(put("/session-data/update/missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("missing", "wf-1"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSessionData_shouldReturn400_whenPathBodySessionIdMismatch() throws Exception {
        mockMvc.perform(put("/session-data/update/path-session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("body-session", "wf-1"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSessionData_shouldReturn409ForOneConcurrentRequest_whenConflictOccurs() throws Exception {
        String sessionId = "session-concurrent";
        CountDownLatch barrier = new CountDownLatch(2);
        AtomicInteger counter = new AtomicInteger(0);

        doAnswer(invocation -> {
            int call = counter.incrementAndGet();
            barrier.countDown();
            if (!barrier.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for concurrent update calls");
            }
            if (call == 2) {
                throw new SessionUpdateConflictException(sessionId, new RuntimeException("conflict"));
            }
            return null;
        }).when(sessionDataService).updateSessionData(eq(sessionId), any(SessionDataDto.class));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<Integer> requestCall = () -> mockMvc.perform(put("/session-data/update/" + sessionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest(sessionId, "wf-1"))))
                    .andReturn()
                    .getResponse()
                    .getStatus();

            Future<Integer> first = executor.submit(requestCall);
            Future<Integer> second = executor.submit(requestCall);

            int s1 = first.get(10, TimeUnit.SECONDS);
            int s2 = second.get(10, TimeUnit.SECONDS);

            int okCount = (s1 == 200 ? 1 : 0) + (s2 == 200 ? 1 : 0);
            int conflictCount = (s1 == 409 ? 1 : 0) + (s2 == 409 ? 1 : 0);

            org.junit.jupiter.api.Assertions.assertEquals(1, okCount);
            org.junit.jupiter.api.Assertions.assertEquals(1, conflictCount);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void updateSessionData_shouldReturnConflictBody_whenConflictOccurs() throws Exception {
        String sessionId = "session-409";
        doThrow(new SessionUpdateConflictException(sessionId, new RuntimeException("conflict")))
                .when(sessionDataService).updateSessionData(eq(sessionId), any(SessionDataDto.class));

        mockMvc.perform(put("/session-data/update/" + sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(sessionId, "wf-1"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode", is("SESSION_UPDATE_CONFLICT")))
                .andExpect(jsonPath("$.errorMessage", is("Session was modified by another transaction.")))
                .andExpect(jsonPath("$.sessionId", is(sessionId)))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    private SessionDataDto buildRequest(String sessionId, String workflowId) {
        SessionDataDto dto = new SessionDataDto();
        dto.setSessionId(sessionId);
        dto.setWorkflowId(workflowId);
        dto.setUpdatedBy("test-user");
        return dto;
    }
}
