package com.corelate.app.repository;

import com.corelate.app.entity.MockAppCertificateFieldMapping;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MockAppCertificateFieldMappingRepository extends JpaRepository<MockAppCertificateFieldMapping, Long> {

    List<MockAppCertificateFieldMapping> findByMockApp_WorkflowId(String workflowId);

    List<MockAppCertificateFieldMapping> findBySourcePath(String sourcePath);

    List<MockAppCertificateFieldMapping> findByMockApp_FormIdAndMockApp_WorkflowId(String formId, @NotBlank String workflowId);

    List<MockAppCertificateFieldMapping> findByMockApp_AppIdAndMockApp_WorkflowIdAndMockApp_FormId(
            @NotBlank String appId,
            @NotBlank String workflowId,
            @NotBlank String formId
    );
}
