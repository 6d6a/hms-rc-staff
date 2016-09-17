package ru.majordomo.hms.rc.staff.exception;

public class ParameterValidateException extends Exception {
    public ParameterValidateException() {};

    public ParameterValidateException(String message) {
        super(message);
    }
}
