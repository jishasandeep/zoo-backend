package com.er.zoo.dto;

import com.er.zoo.enums.SortField;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record RoomRequest(
        String roomId,
        SortField sort,
        Sort.Direction order,
        @Min(0)
        int page,
        @Max(20)
        int size) {

    public RoomRequest{
        if(sort == null) sort = SortField.TITLE;
        if(order == null) order = Sort.Direction.ASC;

        if(size<0) size = 0;
    }

    public Pageable toPageable(){
        return PageRequest.of(page,size,Sort.by(order,sort.name().toLowerCase()));
    }
}
