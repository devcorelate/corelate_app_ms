package com.corelate.app.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class AppAlreadyExistExeption extends RuntimeException {

    public AppAlreadyExistExeption(String message){
        super(message);
    }
}
