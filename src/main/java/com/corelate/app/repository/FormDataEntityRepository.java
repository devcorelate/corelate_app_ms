package com.corelate.app.repository;

import com.corelate.app.entity.FormDataEntity;
import com.corelate.app.entity.PublishLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FormDataEntityRepository extends JpaRepository<FormDataEntity, Long> {
	/**
	 * @param templateId
	 * @return
	 */
	List<FormDataEntity> findByTemplateId(String templateId);
}
