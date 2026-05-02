package com.corelate.app.repository;

import com.corelate.app.entity.MockApp;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MockAppRepository extends JpaRepository<MockApp, Long> {

    Optional<MockApp> findByAppId(String appId);

    @EntityGraph(attributePaths = "certificateFieldMappings")
    Optional<MockApp> findWithCertificateFieldMappingsByAppId(String appId);

    @Override
    @EntityGraph(attributePaths = "certificateFieldMappings")
    List<MockApp> findAll();

    List<MockApp> findByAppIdIn(List<String> appIds);
}
