package com.corelate.app.repository;

import com.corelate.app.entity.MockAppCertificateFieldMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MockAppCertificateFieldMappingRepository extends JpaRepository<MockAppCertificateFieldMapping, Long> {

    List<MockAppCertificateFieldMapping> findByMockApp_FormIdAndMockApp_WorkflowId(String formId, String workflowId);

    List<MockAppCertificateFieldMapping> findByMockApp_WorkflowId(String workflowId);

    default List<MockAppCertificateFieldMapping> findByFormIdAndWorkflowId(String formId, String workflowId) {
        if (formId == null || formId.isBlank()) {
            return findByMockApp_WorkflowId(workflowId);
        }
        return findByMockApp_FormIdAndMockApp_WorkflowId(formId, workflowId);
    }
}
