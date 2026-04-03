package com.corelate.app.repository;

import com.corelate.app.entity.SessionElementData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionElementDataRepository extends JpaRepository<SessionElementData, Long> {

    List<SessionElementData> findByWorkflowId(String workflowId);
}
