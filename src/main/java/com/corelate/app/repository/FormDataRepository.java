package com.corelate.app.repository;


import com.corelate.app.entity.FormData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormDataRepository extends JpaRepository<FormData, Long> {

    /**
     * @param dataId
     * @return
     */
    Optional<FormData> findByDataId(Long dataId);

    /**
     * @param dataId
     */
    @Transactional
    @Modifying
    void deleteByDataId(Long dataId);

    List<FormData> findAllByTemplateIdAndFormId(String templateId, String formId);
}
