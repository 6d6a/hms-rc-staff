package ru.majordomo.hms.rc.staff.exception;

public class ResourceNotFoundException extends Exception {
    public ResourceNotFoundException(){};
    public ResourceNotFoundException(String message) {super(message);}
}
