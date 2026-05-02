package com.corelate.app.repository;

import com.corelate.app.entity.MockAppCertificateFieldMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MockAppCertificateFieldMappingRepository extends JpaRepository<MockAppCertificateFieldMapping, Long> {

    List<MockAppCertificateFieldMapping> findByMockApp_WorkflowId(String workflowId);

    List<MockAppCertificateFieldMapping> findBySourcePath(String sourcePath);
}
