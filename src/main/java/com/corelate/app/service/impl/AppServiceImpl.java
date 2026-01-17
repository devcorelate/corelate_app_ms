package com.corelate.app.service.impl;

import com.corelate.app.dto.*;

import com.corelate.app.entity.FormData;
import com.corelate.app.entity.FormDataEntity;
import com.corelate.app.entity.ListData;
import com.corelate.app.entity.PublishLog;
import com.corelate.app.exeption.ResourceNotFoundException;
import com.corelate.app.mapper.AppMapper;
import com.corelate.app.repository.FormDataEntityRepository;
import com.corelate.app.repository.FormDataRepository;
import com.corelate.app.repository.ListRepository;
import com.corelate.app.repository.PublishLogsRepository;
import com.corelate.app.service.IAppService;
import com.corelate.app.service.client.AppFeignClient;
import com.corelate.app.utility.JwtUtil;
import lombok.AllArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AppServiceImpl implements IAppService {

    private static final Logger log = LoggerFactory.getLogger(AppServiceImpl.class);

    private ListRepository listRepository;
    private FormDataRepository formDataRepository;
    private FormDataEntityRepository formDataEntityRepository;
    private PublishLogsRepository publishLogsRepository;
    private AppFeignClient appFeignClient;
    private final StreamBridge streamBridge;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void createList(ListDto listDto) {
        ListData list = AppMapper.mapToList(listDto, new ListData());

        list.setCreatedAt(LocalDateTime.now());
        list.setCreatedBy("Admin");
        listRepository.save(list);
    }

    @Override
    public void publishApplication(PublishDto publishDto) {
        PublishLog publishLog = new PublishLog();
        publishLog.setApplicationId(publishDto.getApplicationId());
        publishLog.setPublish(publishDto.isPublish());
        publishLog.setPublishAt(LocalDateTime.now());
        publishLog.setPublishBy(publishDto.getPublishBy());
        publishLog.setUsers(publishDto.getUsers());
        publishLog.setVisibility(publishDto.getVisibility());
        publishLogsRepository.save(publishLog);
    }

    /**
     * @param formUpdateRequestDto
     */
    @Override
    public void updateFormData(FormUpdateRequestDto formUpdateRequestDto) {
        List<FormData> formDatas = formDataRepository.findAllByTemplateIdAndFormId(formUpdateRequestDto.getTemplateId(),
                formUpdateRequestDto.getFormId());

        for (UpdatedDataDto updatedData : formUpdateRequestDto.getUpdatedData()) {
            // Check if the entity exists by ID
            FormData existingFormData = formDatas.stream()
                    .filter(fd -> fd.getDataId().equals(updatedData.getId()))
                    .findFirst()
                    .orElse(null);

            if (existingFormData != null) {
                // Update existing entity
//                existingFormData.setData(updatedData.getData()); // Assuming JSON string storage
                formDataRepository.save(existingFormData);
            } else {
                // Create a new entity without generating a new ID
                FormData newFormData = new FormData();
                newFormData.setDataId(updatedData.getId()); // Set ID manually
                newFormData.setTemplateId(formUpdateRequestDto.getTemplateId());
                newFormData.setFormId(formUpdateRequestDto.getFormId());
//                newFormData.setData(updatedData.getData());

                formDataRepository.save(newFormData);
            }
        }
    }

    @Override
    public ListDto fetchList(String ListId) {
        ListData list = listRepository.findByListId(ListId).orElseThrow(
                ()-> new ResourceNotFoundException("List", "Id", ListId)
        );

        ListDto listDto = AppMapper.mapToListDto(list, new ListDto());


        return listDto;
    }

    @Override
    public List<ListDto> fetchAllList() {
        List<ListDto> listDtos = new ArrayList<>();
        List<ListData> forms = listRepository.findByCreatedBy("Admin");

        for (ListData form : forms) {
            ListDto listDto = AppMapper.mapToListDto(form, new ListDto());
            listDtos.add(listDto);
        }

        return listDtos;
    }

    @Override
    public boolean updateList(ListDto listDto) {
        return false;
    }

    @Override
    public boolean deleteList(String ListId) {
        return false;
    }

    @Override
    public boolean updateCommunicationStatus(String formId) {
        boolean isUpdated = false;
        if(formId !=null ){
            ListData list = listRepository.findById(formId).orElseThrow(
                    () -> new ResourceNotFoundException("List", "ListID", formId)
            );
            list.setCommunicationSw(true);
            listRepository.save(list);
            isUpdated = true;
        }
        return isUpdated;
    }

    @Override
    public List<PublishDtoEmail> getLatestUniquePublishedLogs() {
        return publishLogsRepository.findAll().stream()
                .collect(Collectors.toMap(
                        PublishLog::getApplicationId,
                        Function.identity(),
                        (existing, replacement) -> replacement.getPublishAt().isAfter(existing.getPublishAt()) ? replacement : existing
                ))
                .values()
                .stream()
                .map(publishLog -> {
                    PublishDtoEmail dto = new PublishDtoEmail();
                    dto.setApplicationId(publishLog.getApplicationId());
                    dto.setPublish(publishLog.isPublish());
                    dto.setPublishAt(publishLog.getPublishAt());
                    dto.setPublishBy(publishLog.getPublishBy());
                    dto.setVisibility(publishLog.getVisibility() == null ? "private" : publishLog.getVisibility());

                    List<String> emails = new ArrayList<>();
                    List<Long> userIds = Optional.ofNullable(publishLog.getUsers()).orElse(Collections.emptyList());

                    for (Long id : userIds) {
                        try {
                            CustomerDto customerDto = appFeignClient.fetchAccountsById(id);
                            System.out.println("Fetched DTO: " + customerDto);
                            if (customerDto != null && customerDto.getEmail() != null) {
                                emails.add(customerDto.getEmail());
                            }
                        } catch (Exception e) {
                            System.err.println("Error fetching user ID " + id + ": " + e.getMessage());
                        }
                    }

                    dto.setEmails(emails);
                    return dto;
                })
                .collect(Collectors.toList());
    }


    @Override
    public void createFormDataList(FormDataDto formDataDto) {
        FormDataEntity entity = new FormDataEntity();
        entity.setTemplateId(formDataDto.getTemplateId());
        entity.setFormId(formDataDto.getFormId());
        entity.setFormData(formDataDto.getFormData());
        entity.setCreatedBy(formDataDto.getCreatedBy());
        entity.setCreatedAt(LocalDateTime.now());
        formDataEntityRepository.save(entity);
    }

    /**
     * @return
     */
    @Override
    public List<FormDataDto> getAllDataByWorkflowId(String templateId) {
        List<FormDataEntity> entities = formDataEntityRepository.findByTemplateId(templateId);

        return entities.stream().map(entity -> {
            FormDataDto dto = new FormDataDto();
            dto.setTemplateId(entity.getTemplateId());
            dto.setFormId(entity.getFormId());
            dto.setFormData(entity.getFormData());
            return dto;
        }).toList();
    }


}
