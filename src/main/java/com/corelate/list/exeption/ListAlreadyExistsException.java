package com.corelate.list.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class FormAlreadyExistsException extends RuntimeException {

    public FormAlreadyExistsException(String message){
        super(message);
    }
}
