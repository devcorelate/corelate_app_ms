package com.corelate.app.service;
import com.corelate.app.dto.*;
import jakarta.validation.Valid;

import java.util.List;

public interface IAppService {

	/**
	 * @param ListDto - ListDto Object
	 * @return List Details
	 */
	void createList(ListDto ListDto);

	void publishApplication(PublishDto publishDto);

	/**
	 * @param formUpdateRequestDto
	 */
	void updateFormData(FormUpdateRequestDto formUpdateRequestDto);

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


	/**
	 * @param ListId
	 * @return
	 */
	boolean updateCommunicationStatus(String ListId);

	/**
	 * @return List<PublishDtoEmail>
	 */
	List<PublishDtoEmail> getLatestUniquePublishedLogs();


	/**
	 * @param formDataDto
	 */
	void createFormDataList(FormDataDto formDataDto);

	/**
	 * @return
	 */
	List<FormDataDto> getAllDataByWorkflowId(String templateId);
}
