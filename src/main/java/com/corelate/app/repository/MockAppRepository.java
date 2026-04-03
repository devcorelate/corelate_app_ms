package com.corelate.app.repository;

import com.corelate.app.entity.MockApp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MockAppRepository extends JpaRepository<MockApp, Long> {

    Optional<MockApp> findByAppId(String appId);
}
