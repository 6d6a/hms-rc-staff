package ru.majordomo.hms.rc.staff.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ParameterValidateException extends RuntimeException {
    public ParameterValidateException() {};

    public ParameterValidateException(String message) {
        super(message);
    }
}
