package com.rest.webservices.restfulwebservices.apk.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class AlreadyPresentException extends RuntimeException {

    public AlreadyPresentException(String message) {
        super(message);
    }
}
