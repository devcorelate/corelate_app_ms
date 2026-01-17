package com.corelate.app.repository;


import com.corelate.app.entity.ListData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListRepository extends JpaRepository<ListData,String> {

    /**
     * @param listId
     * @return
     */
    Optional<ListData> findByListId(String listId);

    /**
     * @param listId
     */
    @Transactional
    @Modifying
    void deleteByListId(String listId);

    List<ListData> findByCreatedBy(String createBy);
}
