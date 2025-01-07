package com.corelate.list.mapper;


import com.corelate.list.dto.ListDto;
import com.corelate.list.entity.ListData;

public class ListMapper {

    public static ListData mapToList(ListDto listDto, ListData list) {


        list.setListId(listDto.getListId());


        return list;
    }

    public static ListDto mapToListDto(ListData list, ListDto listDto) {

        listDto.setListId(list.getListId());


        return listDto;
    }
}
