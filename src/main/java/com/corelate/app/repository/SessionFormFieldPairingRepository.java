package com.corelate.app.repository;

import com.corelate.app.entity.SessionFormFieldPairing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionFormFieldPairingRepository extends JpaRepository<SessionFormFieldPairing, Long> {

    List<SessionFormFieldPairing> findBySessionId(String sessionId);

    Optional<SessionFormFieldPairing> findBySessionIdAndWorkflowIdAndFormIdAndSourcePathAndTargetField(
            String sessionId,
            String workflowId,
            String formId,
            String sourcePath,
            String targetField
    );
}
