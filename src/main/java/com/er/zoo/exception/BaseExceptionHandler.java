package com.er.zoo.exception;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseExceptionHandler {

    protected Map<String,Object> getExceptionDetails(Exception e){
        var body = new HashMap<String,Object>();
        body.put("timestamp", ZonedDateTime.now());
        body.put("error", e.getMessage());
        return body;
    }
}
