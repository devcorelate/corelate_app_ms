package com.corelate.app.repository;

import com.corelate.app.entity.SessionData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionDataRepository extends JpaRepository<SessionData, Long> {

    Optional<SessionData> findBySessionId(String sessionId);

    void deleteBySessionId(String sessionId);
}
