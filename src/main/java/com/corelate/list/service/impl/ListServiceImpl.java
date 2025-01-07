package com.corelate.list.service.impl;

import com.corelate.list.dto.ListDto;

import com.corelate.list.entity.ListData;
import com.corelate.list.exeption.ResourceNotFoundException;
import com.corelate.list.mapper.ListMapper;
import com.corelate.list.repository.ListRepository;
import com.corelate.list.service.IListService;
import com.corelate.list.utility.JwtUtil;
import lombok.AllArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ListServiceImpl implements IListService {

    private static final Logger log = LoggerFactory.getLogger(ListServiceImpl.class);

    private ListRepository listRepository;
    private final StreamBridge streamBridge;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void createList(ListDto listDto) {
        ListData list = ListMapper.mapToList(listDto, new ListData());

        list.setCreatedAt(LocalDateTime.now());
        list.setCreatedBy("Admin");
        listRepository.save(list);
    }

    @Override
    public ListDto fetchList(String ListId) {
        ListData list = listRepository.findByListId(ListId).orElseThrow(
                ()-> new ResourceNotFoundException("List", "Id", ListId)
        );

        ListDto listDto = ListMapper.mapToListDto(list, new ListDto());


        return listDto;
    }

    @Override
    public List<ListDto> fetchAllList() {
        List<ListDto> listDtos = new ArrayList<>();
        List<ListData> forms = listRepository.findByCreatedBy("Admin");

        for (ListData form : forms) {
            ListDto listDto = ListMapper.mapToListDto(form, new ListDto());
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
}
