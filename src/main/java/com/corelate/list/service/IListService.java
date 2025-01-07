package com.corelate.list.service;
import com.corelate.list.dto.ListDto;

import java.util.List;

public interface IListService {

    /**
     * @param ListDto - ListDto Object
     * @return List Details
     */
    void createList(ListDto ListDto);

    /**
     *     @param ListId - Input List ID
     *     @return List Details
     */
    ListDto fetchList(String ListId);

    /**
     *
     *     @return List of ListDTOs Details
     */
    List<ListDto> fetchAllList();

    /**
     *     @param ListDto - ListDto Object
     *     @return boolean indicating if the update of List detail is successful or not
     */
    boolean updateList(ListDto ListDto);

    /**
     *     @param ListId - ListId Object
     *     @return boolean indicating if the update of Account detail is successful or not
     */
    boolean deleteList(String ListId);


    boolean updateCommunicationStatus(String ListId);
}
