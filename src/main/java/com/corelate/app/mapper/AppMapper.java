package com.corelate.app.mapper;


import com.corelate.app.dto.ListDto;
import com.corelate.app.entity.ListData;

public class AppMapper {

    public static ListData mapToList(ListDto listDto, ListData list) {


        list.setListId(listDto.getListId());


        return list;
    }

    public static ListDto mapToListDto(ListData list, ListDto listDto) {

        listDto.setListId(list.getListId());


        return listDto;
    }
}
