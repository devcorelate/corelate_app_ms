package com.corelate.app.repository;


import com.corelate.app.entity.FormData;
import com.corelate.app.entity.PublishLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PublishLogsRepository extends JpaRepository<PublishLog, Long> {

	/**
	 * @param publishId
	 * @return
	 */
	Optional<PublishLog> findByPublishId(Long publishId);

	/**
	 * @param publishId
	 */
	@Transactional
	@Modifying
	void deleteByPublishId(Long publishId);

	Optional<PublishLog> findFirstByOrderByPublishAtDesc();


}
