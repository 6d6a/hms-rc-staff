package ru.majordomo.hms.rc.staff.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(){};
    public ResourceNotFoundException(String message) {super(message);}
}
