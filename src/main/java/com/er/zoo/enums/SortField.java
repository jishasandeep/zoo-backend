package com.er.zoo.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum SortField {
    TITLE,
    LOCATED,
    CREATED;

/*    @JsonCreator
    public static SortField from(String value){
        return SortField.valueOf(value.toUpperCase());
    }*/
}
